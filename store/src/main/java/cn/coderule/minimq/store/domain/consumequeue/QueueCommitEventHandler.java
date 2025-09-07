package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.cluster.store.CommitEvent;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.cluster.store.domain.commitlog.CommitEventHandler;
import cn.coderule.minimq.domain.domain.cluster.store.domain.consumequeue.ConsumeQueueGateway;

public class QueueCommitEventHandler implements CommitEventHandler {
    private final ConsumeQueueGateway consumeQueueGateway;

    public QueueCommitEventHandler(ConsumeQueueGateway consumeQueueGateway) {
        this.consumeQueueGateway = consumeQueueGateway;
    }

    @Override
    public void handle(CommitEvent event) {
        MessageBO messageBO = event.getMessageBO();
        if (!messageBO.isNormalOrCommitMessage()) {
            return;
        }

        consumeQueueGateway.enqueue(event);
    }
}
