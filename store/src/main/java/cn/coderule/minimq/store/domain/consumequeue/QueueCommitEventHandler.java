package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEventHandler;

public class QueueCommitEventHandler implements CommitEventHandler {
    private final DefaultConsumeQueueFacade consumeQueueFacade;

    public QueueCommitEventHandler(DefaultConsumeQueueFacade consumeQueueFacade) {
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
