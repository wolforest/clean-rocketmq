package cn.coderule.minimq.broker.domain.transaction.check.service;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.DefaultThreadFactory;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.domain.producer.ProducerRegister;
import cn.coderule.minimq.broker.domain.transaction.receipt.Receipt;
import cn.coderule.minimq.broker.domain.transaction.receipt.ReceiptRegistry;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.core.relay.request.TransactionRequest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckService implements Lifecycle {
    private final TransactionConfig transactionConfig;
    private final ReceiptRegistry receiptRegistry;

    private final ExecutorService executor;

    private ProducerRegister producerRegister;

    public CheckService(TransactionConfig transactionConfig, ReceiptRegistry receiptRegistry) {
        this.transactionConfig = transactionConfig;
        this.receiptRegistry = receiptRegistry;

        this.executor = initExecutor();
    }

    public void inject(ProducerRegister producerRegister) {
        this.producerRegister = producerRegister;
    }

    public void check(MessageBO messageBO) {
        executor.execute(() -> {
            try {
                checkAsync(messageBO);
            } catch (Exception e) {
                log.error("check message error", e);
            }
        });
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
        executor.shutdown();
    }

    private void checkAsync(MessageBO messageBO) {
        String producerGroup = messageBO.getProperty(MessageConst.PROPERTY_PRODUCER_GROUP);
        if (StringUtil.isBlank(producerGroup)) {
            log.error("message producer group is null, message: {}", messageBO);
            return;
        }

        RelayService relayService = producerRegister.getRelayService(producerGroup);
        if (relayService == null) {
            log.error("can't find RelayService by producerGroup: {}", producerGroup);
            return;
        }

        registerReceipt(messageBO);

        TransactionRequest request = TransactionRequest.build(messageBO);
        relayService.checkTransaction(request);
    }

    private void registerReceipt(MessageBO messageBO) {
        String topic = messageBO.getTopic();
        Receipt receipt = Receipt.builder()
            .topic(topic)
            .producerGroup(messageBO.getProducerGroup())

            .storeGroup(messageBO.getStoreGroup())
            .messageId(messageBO.getMessageId())
            .transactionId(messageBO.getTransactionId())
            .commitOffset(messageBO.getCommitOffset())
            .queueOffset(messageBO.getQueueOffset())

            .checkTimestamp(System.currentTimeMillis())
            .expireMs(transactionConfig.getReceiptExpireTime())
            .build();
        receiptRegistry.register(receipt);
    }

    private ExecutorService initExecutor() {
        return ThreadUtil.newThreadPoolExecutor(
            transactionConfig.getCheckThreadNum(),
            transactionConfig.getMaxCheckThreadNum(),
            transactionConfig.getKeepAliveTime(),
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(transactionConfig.getCheckQueueCapacity()),
            new DefaultThreadFactory("TransactionCheckThread_"),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
