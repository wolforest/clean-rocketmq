package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.pool.ThreadPoolFactory;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.domain.transaction.Transaction;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.constant.MessageConst;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.enums.code.InvalidCode;
import cn.coderule.minimq.domain.domain.enums.message.CleanupPolicy;
import cn.coderule.minimq.domain.domain.enums.message.MessageType;
import cn.coderule.minimq.domain.domain.exception.InvalidParameterException;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.model.producer.ProduceContext;
import cn.coderule.minimq.domain.service.broker.infra.TopicStore;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.domain.utils.CleanupUtils;
import cn.coderule.minimq.domain.utils.MessageUtils;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageSender implements Lifecycle {
    private final BrokerConfig brokerConfig;
    private final MessageStore messageStore;
    private final ThreadPoolExecutor executor;


    private ProduceHookManager hookManager;
    private QueueSelector queueSelector;
    private TopicStore topicStore;
    private Transaction transaction;

    public MessageSender(BrokerConfig brokerConfig, MessageStore messageStore) {
        this.brokerConfig = brokerConfig;
        this.messageStore = messageStore;
        this.executor = createExecutor();
    }

    private ProduceContext createContext(RequestContext requestContext, MessageBO messageBO) {
        ProduceContext produceContext = ProduceContext.from(requestContext, messageBO);

        selectQueue(produceContext);
        getTopic(produceContext);
        checkCleanupPolicy(produceContext);
        addMessageInfo(produceContext);
        initTransactionInfo(produceContext);

        return produceContext;
    }

    private CompletableFuture<EnqueueResult> storeMessage(ProduceContext context) {
        CompletableFuture<EnqueueResult> future;
        if (context.isPrepareMessage()) {
            future = transaction.prepare(context.getRequestContext(), context.getMessageBO());
        } else {
            future = messageStore.enqueueAsync(context.getMessageBO());
        }

        return future;
    }

    private Consumer<EnqueueResult> sendCallback(ProduceContext context) {
        return result -> {

            hookManager.postProduce(context);
        };
    }

    /**
     * send message
     *
     * @todo static topic checking
     * @todo handle retry or DLQ
     * @param requestContext request context
     * @param messageBO message
     * @return future
     */
    public CompletableFuture<EnqueueResult> send(RequestContext requestContext, MessageBO messageBO) {
        ProduceContext context = createContext(requestContext, messageBO);

        hookManager.preProduce(context);
        CompletableFuture<EnqueueResult> future = storeMessage(context);
        future.thenAcceptAsync(sendCallback(context), executor);

        return future;
    }

    public CompletableFuture<List<EnqueueResult>> send(RequestContext context, List<MessageBO> messageList) {
        if (CollectionUtil.isEmpty(messageList)) {
            return CompletableFuture.completedFuture(List.of());
        }

        List<CompletableFuture<EnqueueResult>> futureList = new ArrayList<>();
        for (MessageBO messageBO : messageList) {
            CompletableFuture<EnqueueResult> future = send(context, messageBO);
            futureList.add(future);
        }

        return combineEnqueueResult(futureList);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        this.executor.shutdown();
    }

    private ThreadPoolExecutor createExecutor() {
        return ThreadPoolFactory.create(
            brokerConfig.getProducerThreadNum(),
            brokerConfig.getProducerThreadNum(),
            1,
            TimeUnit.MINUTES,
            "producer-thread-pool",
            brokerConfig.getProducerQueueCapacity()
        );
    }

    private CompletableFuture<List<EnqueueResult>> combineEnqueueResult(List<CompletableFuture<EnqueueResult>> futureList) {
        CompletableFuture<Void> all = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));
        return all.thenApply(v -> {
            List<EnqueueResult> resultList = new ArrayList<>();
            for (CompletableFuture<EnqueueResult> future : futureList) {
                addEnqueueResult(resultList, future);
            }
            return resultList;
        });
    }

    private void addEnqueueResult(List<EnqueueResult> resultList, CompletableFuture<EnqueueResult> future) {
        try {
            EnqueueResult enqueueResult = future.get();
            resultList.add(enqueueResult);
        } catch (Throwable t) {
            log.error("produce message error", t);
            resultList.add(EnqueueResult.failure());
        }
    }



    private void selectQueue(ProduceContext produceContext) {
        RequestContext context = produceContext.getRequestContext();
        MessageBO message = produceContext.getMessageBO();

        MessageQueue messageQueue = queueSelector.select(context, message);
        message.setQueueId(messageQueue.getQueueId());

        produceContext.setMessageQueue(messageQueue);
    }

    private void getTopic(ProduceContext produceContext) {
        String topicName = produceContext.getMessageBO().getTopic();
        Topic topic = topicStore.getTopic(topicName);
        if (topic == null) {
            throw new InvalidParameterException(InvalidCode.ILLEGAL_TOPIC, "Topic not exists");
        }
    }

    private void checkCleanupPolicy(ProduceContext produceContext) {
        CleanupPolicy policy = CleanupUtils.getDeletePolicy(
            Optional.of(
                produceContext.getTopic()
            )
        );

        if (policy == CleanupPolicy.COMPACTION) {
            return;
        }

        MessageBO message = produceContext.getMessageBO();
        if (policy == CleanupPolicy.DELETE && StringUtil.isBlank(message.getKeys())) {
            throw new InvalidParameterException(
                InvalidCode.ILLEGAL_MESSAGE_PROPERTY_KEY, "required message key is missing"
            );
        }
    }

    private void setTagsCode(ProduceContext produceContext) {
        MessageBO message = produceContext.getMessageBO();
        Topic topic = produceContext.getTopic();

        long tagCode = MessageUtils.getTagsCode(topic.getTagType(), message.getTags());
        message.setTagsCode(tagCode);
    }

    private void addMessageInfo(ProduceContext produceContext) {
        MessageBO message = produceContext.getMessageBO();

        setTagsCode(produceContext);
        message.setStoreHost(new InetSocketAddress(brokerConfig.getHost(), brokerConfig.getPort()));
        message.setClusterName(brokerConfig.getCluster());
    }

    private void initTransactionInfo(ProduceContext produceContext) {
        MessageBO message = produceContext.getMessageBO();
        String flag = message.getProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED);
        boolean isPrepared = StringUtil.isBlank(flag) || Boolean.parseBoolean(flag);

        if (isPrepared) {
            produceContext.setMsgType(MessageType.PREPARE);
        }
    }


}
