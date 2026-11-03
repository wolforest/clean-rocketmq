package com.wolfmq.store.domain.mq.queue;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.domain.mq.dto.EnqueueResult;
import com.wolfmq.common.domain.mq.dto.EnqueueFuture;
import com.wolfmq.common.core.lock.queue.EnqueueLock;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;
import com.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnqueueService {
    private final StoreConfig storeConfig;
    private final ConsumeQueueFacade consumeQueueFacade;
    private final CommitLogFacade commitLogFacade;

    private CommitLogSynchronizer commitLogSynchronizer;
    private final EnqueueLock enqueueLock;

    public EnqueueService(
        StoreConfig storeConfig,
        CommitLogFacade commitLogFacade,
        ConsumeQueueFacade consumeQueueFacade) {

        this.storeConfig = storeConfig;
        this.commitLogFacade = commitLogFacade;
        this.consumeQueueFacade = consumeQueueFacade;

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
            EnqueueFuture result = commitLogFacade.insert(messageBO);

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

        long queueOffset = consumeQueueFacade.assignOffset(messageBO.getTopic(), messageBO.getQueueId());
        messageBO.setQueueOffset(queueOffset);
    }

    private void increaseConsumeOffset(MessageBO messageBO) {
        if (!storeConfig.isAssignConsumeOffset()) {
            return;
        }

        consumeQueueFacade.increaseOffset(messageBO.getTopic(), messageBO.getQueueId());
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
