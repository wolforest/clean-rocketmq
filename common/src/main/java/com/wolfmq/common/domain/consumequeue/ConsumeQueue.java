package com.wolfmq.common.domain.consumequeue;

import com.wolfmq.common.domain.commitlog.dispatch.CommitEvent;
import java.util.List;
import java.util.Map;

public interface ConsumeQueue {
    QueueType getQueueType();
    String getTopic();
    int getQueueId();
    int getUnitSize();

    void enqueue(CommitEvent event);
    QueueUnit get(long index);
    List<QueueUnit> get(long index, int num);

    long getMinOffset();
    void setMinOffset(long offset);
    long getMaxOffset();
    void setMaxOffset(long maxOffset);

    long increaseOffset();
    long rollToOffset(long offset);

    void load();
    void destroy();
}
