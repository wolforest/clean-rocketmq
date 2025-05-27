package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.model.cluster.store.CommitLogEvent;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLogHandler;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;

public class QueueCommitLogHandler implements CommitLogHandler {
    private final ConsumeQueueGateway consumeQueueGateway;

    public QueueCommitLogHandler(ConsumeQueueGateway consumeQueueGateway) {
        this.consumeQueueGateway = consumeQueueGateway;
    }

    @Override
    public void handle(CommitLogEvent event) {
        consumeQueueGateway.enqueue(event);
    }
}
