package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.core.lock.queue.EnqueueLock;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.server.processor.CommitLogSynchronizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnqueueService {
    private final StoreConfig storeConfig;
    private final ConsumeQueueGateway consumeQueueGateway;
    private final CommitLog commitLog;

    private CommitLogSynchronizer commitLogSynchronizer;
    private final EnqueueLock enqueueLock;

    public EnqueueService(
        StoreConfig storeConfig,
        CommitLog commitLog,
        ConsumeQueueGateway consumeQueueGateway) {

        this.storeConfig = storeConfig;
        this.consumeQueueGateway = consumeQueueGateway;
        this.commitLog = commitLog;

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
            EnqueueFuture result = commitLog.insert(messageBO);

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

        long queueOffset = consumeQueueGateway.assignOffset(messageBO.getTopic(), messageBO.getQueueId());
        messageBO.setQueueOffset(queueOffset);
    }

    private void increaseConsumeOffset(MessageBO messageBO) {
        if (!storeConfig.isAssignConsumeOffset()) {
            return;
        }

        consumeQueueGateway.increaseOffset(messageBO.getTopic(), messageBO.getQueueId());
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
            StoreConfig config = StoreContext.getBean(StoreConfig.class);
            int timeout = config.getSyncFlushTimeout() + 5 * 1000;
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("enqueue error:", e);
            return EnqueueResult.failure();
        }
    }
}
