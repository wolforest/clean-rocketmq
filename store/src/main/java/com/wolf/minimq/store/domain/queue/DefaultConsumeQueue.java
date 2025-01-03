package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.model.bo.MessageBO;
import java.util.List;

public class DefaultConsumeQueue implements ConsumeQueue {
    @Override
    public void enqueue(CommitLogEvent event) {

    }

    @Override
    public QueueUnit fetch(String topic, int queueId, long offset) {
        return null;
    }

    @Override
    public List<QueueUnit> fetch(String topic, int queueId, long offset, int num) {
        return List.of();
    }

    @Override
    public void assignOffset(MessageBO messageBO) {

    }

    @Override
    public void increaseOffset(MessageBO messageBO) {

    }

    @Override
    public long getMinOffset(String topic, int queueId) {
        return 0L;
    }

    @Override
    public long getMaxOffset(String topic, int queueId) {
        return 0L;
    }
}
