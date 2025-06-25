package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.model.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.cluster.store.InsertFuture;
import cn.coderule.minimq.domain.core.lock.queue.EnqueueLock;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.commitlog.CommitLogSynchronizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnqueueService {
    private final ConsumeQueueGateway consumeQueueGateway;
    private final CommitLogSynchronizer commitLogSynchronizer;
    private final CommitLog commitLog;

    private final EnqueueLock enqueueLock;

    public EnqueueService(
        CommitLog commitLog,
        ConsumeQueueGateway consumeQueueGateway,
        CommitLogSynchronizer commitLogSynchronizer) {

        this.consumeQueueGateway = consumeQueueGateway;
        this.commitLogSynchronizer = commitLogSynchronizer;
        this.commitLog = commitLog;

        this.enqueueLock = new EnqueueLock();
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
        enqueueLock.lock(messageBO.getTopic(), messageBO.getQueueId());
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
            enqueueLock.unlock(messageBO.getTopic(), messageBO.getQueueId());
        }
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
