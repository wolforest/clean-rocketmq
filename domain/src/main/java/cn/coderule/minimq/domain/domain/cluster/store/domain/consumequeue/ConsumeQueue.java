package cn.coderule.minimq.domain.domain.cluster.store.domain.consumequeue;

import cn.coderule.minimq.domain.core.enums.store.QueueType;
import cn.coderule.minimq.domain.domain.cluster.store.CommitEvent;
import cn.coderule.minimq.domain.domain.cluster.store.QueueUnit;
import cn.coderule.minimq.domain.domain.cluster.store.infra.MappedFileQueue;
import java.util.List;

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

    long rollToOffset(long offset);
    long getMaxCommitLogOffset();
    void setMaxCommitLogOffset(long maxCommitLogOffset);
    long increaseOffset();

    MappedFileQueue getMappedFileQueue();
    void destroy();
}
