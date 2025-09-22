package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;
import cn.coderule.minimq.domain.domain.transaction.OffsetQueue;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BatchCommitService extends ServiceThread {
    private final TransactionConfig transactionConfig;
    private final CommitBuffer commitBuffer;
    private final MQStore mqStore;
    private final MessageFactory messageFactory;

    private long wakeupTime;

    public BatchCommitService(
        TransactionConfig transactionConfig,
        CommitBuffer commitBuffer,
        MessageFactory messageFactory,
        MQStore mqStore
    ) {
        this.transactionConfig = transactionConfig;

        this.commitBuffer = commitBuffer;
        this.messageFactory = messageFactory;

        this.mqStore = mqStore;
        initWakeupTime();
    }

    @Override
    public String getServiceName() {
        return BatchCommitService.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            long interval = calculateCommitInterval();
            if (interval > 0) {
                this.await(interval);
            }

            commit();
        }

        log.info("{} service stopped", this.getServiceName());
    }

    private void initWakeupTime() {
        long now = System.currentTimeMillis();
        long interval = transactionConfig.getBatchCommitInterval();

        this.wakeupTime = now + interval;
    }

    private long calculateCommitInterval() {
        long now = System.currentTimeMillis();
        long interval = wakeupTime - now;
        if (interval <= 0) {
            interval = 0;
        }

        return interval;
    }

    private void commit() {
        BatchCommitContext context = new BatchCommitContext();

        try {
            buildOperationMessage(context);
            enqueueOperationMessage(context);
            refreshWakeupTime(context);
        } catch (Throwable e) {
            log.error("{} Commit error", getServiceName(), e);
            this.wakeupTime = 0;
        }
    }

    private void buildOperationMessage(BatchCommitContext context) {
        for (Map.Entry<Integer, OffsetQueue> entry : commitBuffer.getOffsetEntrySet()) {
            buildOperationMessage(context, entry);
        }
    }

    private void buildOperationMessage(BatchCommitContext context, Map.Entry<Integer, OffsetQueue> entry) {
        int queueId = entry.getKey();
        OffsetQueue offsetQueue = entry.getValue();
        if (shouldSkip(context, offsetQueue)) {
            return;
        }

        MessageQueue operationQueue = commitBuffer.getOperationQueue(queueId);
        if (operationQueue == null) {
            log.error("can't find operation queue: queueId: {}", queueId);
            return;
        }

        MessageBO messageBO = messageFactory.createOperationMessage(offsetQueue, operationQueue);
        if (messageBO == null) {
            return;
        }

        context.add(messageBO);
        context.updateFirstTime(offsetQueue.getLastWriteTime());

        if (offsetQueue.getTotalSize() >= transactionConfig.getMaxCommitMessageLength()) {
            context.setOverflow(true);
        }
    }

    private boolean shouldSkip(BatchCommitContext context, OffsetQueue offsetQueue) {
        if (offsetQueue.isEmpty()) return true;

        long interval = transactionConfig.getBatchCommitInterval();
        int maxSize = transactionConfig.getMaxCommitMessageLength();

        boolean notFull = offsetQueue.getTotalSize() < maxSize;
        boolean notExpired = context.getStartTime() - offsetQueue.getLastWriteTime() < interval;

        return notFull && notExpired;
    }

    private void enqueueOperationMessage(BatchCommitContext context) {
        if (context.noMessage()) {
            return;
        }

        for (Map.Entry<Integer, MessageBO> entry : context.getSendEntrySet()) {
            EnqueueRequest request = EnqueueRequest.create(entry.getValue());
            EnqueueResult result = mqStore.enqueue(request);

            if (!result.isSuccess()) {
                log.error("{} enqueue operation message failed, message: {}, result: {}",
                    getServiceName(), entry.getValue(), result);
            }
        }
    }

    private void refreshWakeupTime(BatchCommitContext context) {

    }
}
