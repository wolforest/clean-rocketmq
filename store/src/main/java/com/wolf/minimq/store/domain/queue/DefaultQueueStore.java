package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import com.wolf.minimq.domain.service.store.domain.QueueStore;
import java.util.List;

public class DefaultQueueStore implements QueueStore {
    @Override
    public String getTopic() {
        return "";
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
