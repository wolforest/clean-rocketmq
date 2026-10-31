package com.wolfmq.store.domain.consumequeue;

import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.domain.domain.commitlog.dispatch.CommitHandler;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueProxy;

/**
 * @renamed from QueueCommitEventHandler to ConsumeQueueCommitHandler
 */
public class ConsumeQueueCommitHandler implements CommitHandler {
    private final ConsumeQueueProxy consumeQueueProxy;

    public ConsumeQueueCommitHandler(ConsumeQueueProxy consumeQueueProxy) {
        this.consumeQueueProxy = consumeQueueProxy;
    }

    @Override
    public void handle(CommitEvent event) {
        MessageBO messageBO = event.getMessageBO();
        if (!messageBO.isNormalOrCommitMessage()) {
            return;
        }

        consumeQueueProxy.enqueue(event);
    }
}
