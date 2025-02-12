package cn.coderule.minimq.store.domain.consumequeue;

import cn.coderule.minimq.domain.model.bo.CommitLogEvent;
import cn.coderule.minimq.domain.service.store.domain.CommitLogHandler;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueueStore;

public class QueueCommitLogHandler implements CommitLogHandler {
    private final ConsumeQueueStore consumeQueueStore;

    public QueueCommitLogHandler(ConsumeQueueStore consumeQueueStore) {
        this.consumeQueueStore = consumeQueueStore;
    }

    @Override
    public void handle(CommitLogEvent event) {
        consumeQueueStore.enqueue(event);
    }
}
