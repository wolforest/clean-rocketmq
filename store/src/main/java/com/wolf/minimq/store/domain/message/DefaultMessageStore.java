package com.wolf.minimq.store.domain.message;

import com.wolf.minimq.domain.config.StoreConfig;
import com.wolf.minimq.domain.enums.EnqueueStatus;
import com.wolf.minimq.domain.utils.lock.TopicQueueLock;
import com.wolf.minimq.domain.model.Message;
import com.wolf.minimq.domain.service.store.domain.CommitLog;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.service.store.domain.MessageStore;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.store.server.StoreContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMessageStore implements MessageStore {
    private final TopicQueueLock topicQueueLock;

    public DefaultMessageStore() {
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
        String topicKey = getTopicKey(messageBO);
        messageBO.setTopicKey(topicKey);

        topicQueueLock.lock(topicKey);
        try {
            ConsumeQueue consumeQueue = StoreContext.getBean(ConsumeQueue.class);
            consumeQueue.assignOffset(messageBO);

            CommitLog commitLog = StoreContext.getBean(CommitLog.class);
            CompletableFuture<EnqueueResult> result = commitLog.insert(messageBO);

            consumeQueue.increaseOffset(messageBO);
            return result;
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new EnqueueResult(EnqueueStatus.UNKNOWN_ERROR));
        } finally {
            topicQueueLock.unlock(topicKey);
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

    private String getTopicKey(MessageBO messageBO) {
        return messageBO.getTopic() + '-' + messageBO.getQueueId();
    }
}
