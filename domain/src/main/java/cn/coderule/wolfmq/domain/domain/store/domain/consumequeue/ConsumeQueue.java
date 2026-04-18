package cn.coderule.wolfmq.domain.domain.store.domain.consumequeue;

import cn.coderule.wolfmq.domain.core.enums.store.QueueType;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitEvent;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

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

    void setCommitOffsetMap(ConcurrentMap<Integer, Long> map);
    Map<Integer, Long> getCommitOffsetMap();
    Long getCommitOffsetByShardId(int shardId);
    void setCommitOffsetByShardId(int shardId, long offset);

    long getMaxCommitOffset();
    void setMaxCommitOffset(long maxCommitOffset);

    MappedFileQueue getMappedFileQueue();

    void load();
    void flush(int minPages);
    void destroy();

    default void flush() {
        flush(0);
    }
}
