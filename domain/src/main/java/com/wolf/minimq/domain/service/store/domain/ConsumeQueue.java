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
    int getUnitSize();

    void enqueue(CommitLogEvent event);
    QueueUnit fetch(long index);
    List<QueueUnit> fetch(long index, int num);

    long getMinOffset();
    void setMinOffset(long offset);
    long getMaxOffset();
    void setMaxOffset(long maxOffset);
    long getMaxCommitLogOffset();
    void setMaxCommitLogOffset(long maxCommitLogOffset);
    long increaseOffset();

    MappedFileQueue getMappedFileQueue();
}
