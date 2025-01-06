package com.wolf.minimq.store.domain.consumequeue;

import com.wolf.minimq.domain.enums.QueueType;
import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorConsumeQueue implements ConsumeQueue {
    public static final ErrorConsumeQueue INSTANCE = new ErrorConsumeQueue();

    public static ErrorConsumeQueue singleton() {
        log.error("no such queue store");
        return INSTANCE;
    }

    @Override
    public QueueType getQueueType() {
        return QueueType.ERROR;
    }

    @Override
    public String getTopic() {
        return "NO_SUCH_TOPIC";
    }

    @Override
    public int getQueueId() {
        return 0;
    }

    @Override
    public void enqueue(CommitLogEvent event) {

    }

    @Override
    public QueueUnit fetch(long offset) {
        return null;
    }

    @Override
    public List<QueueUnit> fetch(long offset, int num) {
        return List.of();
    }

    @Override
    public long getMinOffset() {
        return 0;
    }

    @Override
    public long getMaxOffset() {
        return 0;
    }

    @Override
    public long getCurrentOffset() {
        return 0;
    }

    @Override
    public long increaseOffset() {
        return 0;
    }
}
