package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.enums.QueueType;
import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import com.wolf.minimq.domain.service.store.infra.MappedFileQueue;
import java.util.List;

public interface ConsumeQueue {
    QueueType getQueueType();
    String getTopic();
    int getQueueId();

    void enqueue(CommitLogEvent event);
    QueueUnit fetch(long offset);
    List<QueueUnit> fetch(long offset, int num);

    long getMinOffset();
    long getMaxOffset();
    long increaseOffset();

    MappedFileQueue getMappedFileQueue();
}
