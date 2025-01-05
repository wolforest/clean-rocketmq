package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.service.store.domain.CommitLogHandler;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueueStore;

public class QueueCommitLogHandler implements CommitLogHandler {
    private final ConsumeQueueStore consumeQueueStore;

    public QueueCommitLogHandler(ConsumeQueueStore consumeQueueStore) {
        this.consumeQueueStore = consumeQueueStore;
    }

    @Override
    public void handle(CommitLogEvent event) {

    }
}
