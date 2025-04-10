package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.domain.model.CommitLogEvent;
import cn.coderule.minimq.domain.service.store.domain.CommitLogHandler;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueueGateway;

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
