package cn.coderule.minimq.store.domain.mq;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.lock.queue.ConsumeQueueLock;
import cn.coderule.minimq.domain.domain.model.cluster.store.QueueUnit;
import cn.coderule.minimq.domain.domain.dto.InsertFuture;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.domain.lock.queue.TopicQueueLock;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.MQService;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.commitlog.CommitLogSynchronizer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMQService implements MQService {
    private final MessageConfig messageConfig;
    private final ConsumeQueueGateway consumeQueueGateway;
    private final CommitLogSynchronizer commitLogSynchronizer;
    private final CommitLog commitLog;

    private final TopicQueueLock topicQueueLock;
    private final ConsumeQueueLock consumeQueueLock;

    public DefaultMQService(
        MessageConfig messageConfig,
        CommitLog commitLog,
        ConsumeQueueGateway consumeQueueGateway,
        CommitLogSynchronizer commitLogSynchronizer,
        ConsumeQueueLock consumeQueueLock) {

        this.messageConfig = messageConfig;
        this.commitLog = commitLog;
        this.consumeQueueGateway = consumeQueueGateway;
        this.commitLogSynchronizer = commitLogSynchronizer;

        this.topicQueueLock = new TopicQueueLock();
        this.consumeQueueLock = consumeQueueLock;
    }

    /**
     * enqueue single/multi message
     *  - assign consumeQueue offset
     *  - append commitLog
     *  - increase consumeQueue offset
     *
     * @param messageBO messageContext
     * @return EnqueueResult
     */
    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return waitForResult(enqueueAsync(messageBO));
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        topicQueueLock.lock(messageBO.getTopic(), messageBO.getQueueId());
        try {
            long queueOffset = consumeQueueGateway.assignOffset(messageBO.getTopic(), messageBO.getQueueId());
            messageBO.setQueueOffset(queueOffset);

            InsertFuture result = commitLog.insert(messageBO);

            if (result.isInsertSuccess()) {
                consumeQueueGateway.increaseOffset(messageBO.getTopic(), messageBO.getQueueId());
            }

            return commitLogSynchronizer.sync(result);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(EnqueueResult.failure());
        } finally {
            topicQueueLock.unlock(messageBO.getTopic(), messageBO.getQueueId());
        }
    }

    @Override
    public DequeueResult dequeue(String group, String topic, int queueId, int num) {
        if (!consumeQueueLock.tryLock(group, topic, queueId)) {
            return DequeueResult.lockFailed();
        }

        return null;
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset) {
        return get(topic, queueId, offset, 1);
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset, int num) {
        GetRequest request = GetRequest.builder()
            .topic(topic)
            .queueId(queueId)
            .offset(offset)
            .num(num)
            .maxSize(messageConfig.getMaxSize())
            .build();
        return get(request);
    }

    @Override
    public DequeueResult get(GetRequest request) {
        List<QueueUnit> unitList = consumeQueueGateway.get(
            request.getTopic(), request.getQueueId(), request.getOffset(), request.getNum()
        );

        if (CollectionUtil.isEmpty(unitList)) {
            return DequeueResult.notFound();
        }

        return getByUnitList(unitList);
    }

    private DequeueResult getByUnitList(@NonNull List<QueueUnit> unitList) {
        DequeueResult result = new DequeueResult();
        MessageBO messageBO;
        for (QueueUnit unit : unitList) {
            messageBO = commitLog.select(unit.getCommitLogOffset(), unit.getUnitSize());
            if (messageBO == null) {
                continue;
            }

            result.addMessage(messageBO);
        }

        return result;
    }

    @Override
    public MessageBO getMessage(String topic, int queueId, long offset) {
        List<MessageBO> messageList = getMessage(topic, queueId, offset, 1);

        return CollectionUtil.isEmpty(messageList)
            ? null
            : messageList.get(0);
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {
        DequeueResult result = get(topic, queueId, offset, num);
        return result.getMessageList();
    }

    private EnqueueResult waitForResult(CompletableFuture<EnqueueResult> future) {
        try {
            StoreConfig config = StoreContext.getBean(StoreConfig.class);
            int timeout = config.getSyncFlushTimeout() + 5 * 1000;
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("enqueue error:", e);
            return EnqueueResult.failure();
        }
    }
}
