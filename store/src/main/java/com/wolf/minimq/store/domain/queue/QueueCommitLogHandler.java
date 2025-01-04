package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.service.store.domain.CommitLogHandler;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;

public class QueueCommitLogHandler implements CommitLogHandler {
    private final ConsumeQueue consumeQueue;

    public QueueCommitLogHandler(ConsumeQueue consumeQueue) {
        this.consumeQueue = consumeQueue;
    }

    @Override
    public void handle(CommitLogEvent event) {

    }
}
