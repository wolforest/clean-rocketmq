package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEventHandler;
import cn.coderule.minimq.domain.domain.store.domain.consumequeue.ConsumeQueueFacade;

public class QueueCommitEventHandler implements CommitEventHandler {
    private final ConsumeQueueFacade consumeQueueFacade;

    public QueueCommitEventHandler(ConsumeQueueFacade consumeQueueFacade) {
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
