package com.wolfmq.store.domain.mq.queue;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import com.wolfmq.domain.domain.mq.dto.EnqueueFuture;
import com.wolfmq.domain.core.lock.queue.EnqueueLock;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.store.domain.commitlog.log.CommitLogProxy;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueProxy;
import com.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnqueueService {
    private final StoreConfig storeConfig;
    private final ConsumeQueueProxy consumeQueueProxy;
    private final CommitLogProxy commitLogProxy;

    private CommitLogSynchronizer commitLogSynchronizer;
    private final EnqueueLock enqueueLock;

    public EnqueueService(
        StoreConfig storeConfig,
        CommitLogProxy commitLogProxy,
        ConsumeQueueProxy consumeQueueProxy) {

        this.storeConfig = storeConfig;
        this.commitLogProxy = commitLogProxy;
        this.consumeQueueProxy = consumeQueueProxy;

        this.enqueueLock = new EnqueueLock();
    }

    public void inject(CommitLogSynchronizer commitLogSynchronizer) {
        this.commitLogSynchronizer = commitLogSynchronizer;
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
    public EnqueueResult enqueue(MessageBO messageBO) {
        return waitForResult(enqueueAsync(messageBO));
    }

    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        lockMessageQueue(messageBO.getTopic(), messageBO.getQueueId());

        try {
            assignConsumeOffset(messageBO);
            EnqueueFuture result = commitLogProxy.insert(messageBO);

            if (result.isInsertSuccess()) {
                increaseConsumeOffset(messageBO);
            }

            return commitLogSynchronizer.sync(result);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(EnqueueResult.failure());
        } finally {
            unlockMessageQueue(messageBO.getTopic(), messageBO.getQueueId());
        }
    }

    private void assignConsumeOffset(MessageBO messageBO) {
        if (!storeConfig.isAssignConsumeOffset()) {
            messageBO.setQueueOffset(-1);
            return;
        }

        long queueOffset = consumeQueueProxy.assignOffset(messageBO.getTopic(), messageBO.getQueueId());
        messageBO.setQueueOffset(queueOffset);
    }

    private void increaseConsumeOffset(MessageBO messageBO) {
        if (!storeConfig.isAssignConsumeOffset()) {
            return;
        }

        consumeQueueProxy.increaseOffset(messageBO.getTopic(), messageBO.getQueueId());
    }

    private void lockMessageQueue(String topic, int queueId) {
        if (!storeConfig.isAssignConsumeOffset()) {
            return;
        }

        enqueueLock.lock(topic, queueId);
    }

    private void unlockMessageQueue(String topic, int queueId) {
        if (!storeConfig.isAssignConsumeOffset()) {
            return;
        }

        enqueueLock.unlock(topic, queueId);
    }

    private EnqueueResult waitForResult(CompletableFuture<EnqueueResult> future) {
        try {
            int timeout = storeConfig.getSyncFlushTimeout() + 5 * 1000;
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("enqueue error:", e);
            return EnqueueResult.failure();
        }
    }
}
