package com.wolf.minimq.store.domain.queue;

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
    public QueueUnit fetch(long offset) {
        return null;
    }

    @Override
    public List<QueueUnit> fetch(long offset, int num) {
        return List.of();
    }
}
