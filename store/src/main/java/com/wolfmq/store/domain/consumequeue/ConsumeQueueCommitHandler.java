package com.wolfmq.store.domain.consumequeue;

import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.commitlog.dispatch.CommitEvent;
import com.wolfmq.common.domain.commitlog.dispatch.CommitHandler;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;

/**
 * @renamed from QueueCommitEventHandler to ConsumeQueueCommitHandler
 */
public class ConsumeQueueCommitHandler implements CommitHandler {
    private final ConsumeQueueFacade consumeQueueFacade;

    public ConsumeQueueCommitHandler(ConsumeQueueFacade consumeQueueFacade) {
        this.consumeQueueFacade = consumeQueueFacade;
    }

    @Override
    public void handle(CommitEvent event) {
        MessageBO messageBO = event.getMessageBO();
        if (!messageBO.isNormalOrCommitMessage()) {
            return;
        }

        consumeQueueFacade.enqueue(event);
    }
}
