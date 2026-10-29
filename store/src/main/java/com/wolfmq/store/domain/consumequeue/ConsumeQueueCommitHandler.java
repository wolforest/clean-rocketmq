package com.wolfmq.store.domain.consumequeue;

import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.domain.domain.commitlog.dispatch.CommitHandler;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;

/**
 * @renamed from QueueCommitEventHandler to ConsumeQueueCommitHandler
 */
public class ConsumeQueueCommitHandler implements CommitHandler {
    private final ConsumeQueueManager consumeQueueManager;

    public ConsumeQueueCommitHandler(ConsumeQueueManager consumeQueueManager) {
        this.consumeQueueManager = consumeQueueManager;
    }

    @Override
    public void handle(CommitEvent event) {
        MessageBO messageBO = event.getMessageBO();
        if (!messageBO.isNormalOrCommitMessage()) {
            return;
        }

        consumeQueueManager.enqueue(event);
    }
}
