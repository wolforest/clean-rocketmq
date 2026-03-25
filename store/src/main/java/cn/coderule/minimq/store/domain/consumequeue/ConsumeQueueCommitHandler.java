package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitHandler;
import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueManager;

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
