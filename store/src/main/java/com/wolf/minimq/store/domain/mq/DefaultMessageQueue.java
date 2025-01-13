package com.wolf.minimq.store.domain.mq;

import com.wolf.minimq.domain.config.MessageConfig;
import com.wolf.minimq.domain.config.StoreConfig;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import com.wolf.minimq.domain.utils.lock.TopicQueueLock;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueueStore;
import com.wolf.minimq.domain.service.store.domain.MessageQueue;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.store.server.StoreContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMessageQueue implements MessageQueue {
    private final TopicQueueLock topicQueueLock;
    private final MessageConfig messageConfig;
    private final ConsumeQueueStore consumeQueueStore;
    private final CommitLog commitLog;

    public DefaultMessageQueue(
        MessageConfig messageConfig,
        CommitLog commitLog,
        ConsumeQueueStore consumeQueueStore) {

        this.messageConfig = messageConfig;
        this.commitLog = commitLog;
        this.consumeQueueStore = consumeQueueStore;

        this.topicQueueLock = new TopicQueueLock();
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
            consumeQueueStore.assignOffset(messageBO);
            CompletableFuture<EnqueueResult> result = commitLog.insert(messageBO);
            consumeQueueStore.increaseOffset(messageBO);
            return result;
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new EnqueueResult(EnqueueStatus.UNKNOWN_ERROR));
        } finally {
            topicQueueLock.unlock(messageBO.getTopic(), messageBO.getQueueId());
        }
    }

    private EnqueueResult waitForResult(CompletableFuture<EnqueueResult> future) {
        try {
            StoreConfig config = StoreContext.getBean(StoreConfig.class);
            int timeout = config.getSyncFlushTimeout() + 5 * 1000;
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("enqueue error:", e);
            return new EnqueueResult(EnqueueStatus.UNKNOWN_ERROR);
        }
    }
}
