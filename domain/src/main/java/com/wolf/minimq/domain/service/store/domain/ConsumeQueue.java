package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.bo.CommitLogEvent;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.model.bo.QueueUnit;
import java.util.List;

public interface ConsumeQueue {
    void enqueue(CommitLogEvent event);
    QueueUnit fetch(String topic, int queueId, long offset);
    List<QueueUnit> fetch(String topic, int queueId, long offset, int num);

    void assignOffset(MessageBO messageBO);
    void increaseOffset(MessageBO messageBO);

    Long getMinOffset(String topic, int queueId);
    Long getMaxOffset(String topic, int queueId);
}
