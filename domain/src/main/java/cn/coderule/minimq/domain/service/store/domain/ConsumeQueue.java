package cn.coderule.minimq.domain.service.store.domain;

import cn.coderule.minimq.domain.enums.QueueType;
import cn.coderule.minimq.domain.model.CommitLogEvent;
import cn.coderule.minimq.domain.model.QueueUnit;
import cn.coderule.minimq.domain.service.store.infra.MappedFileQueue;
import java.util.List;

public interface ConsumeQueue {
    QueueType getQueueType();
    String getTopic();
    int getQueueId();
    int getUnitSize();

    void enqueue(CommitLogEvent event);
    QueueUnit get(long index);
    List<QueueUnit> get(long index, int num);

    long getMinOffset();
    void setMinOffset(long offset);
    long getMaxOffset();
    void setMaxOffset(long maxOffset);
    long getMaxCommitLogOffset();
    void setMaxCommitLogOffset(long maxCommitLogOffset);
    long increaseOffset();

    MappedFileQueue getMappedFileQueue();
}
