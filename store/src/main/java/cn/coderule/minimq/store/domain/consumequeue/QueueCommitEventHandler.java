package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEventHandler;
import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueManager;

public class QueueCommitEventHandler implements CommitEventHandler {
    private final ConsumeQueueManager consumeQueueManager;

    public QueueCommitEventHandler(ConsumeQueueManager consumeQueueManager) {
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
