package com.wolfmq.common.domain.offset.checkpoint;

public interface CheckpointService {
    void setCommitOffset(int shard, long commitOffset);

    Long getCommitOffset(int shard);

    void setDispatchedOffset(int shard, long commitOffset);

    Long getDispatchedOffset(int shard);

    void setMasterOffset(int shard, long commitOffset);

    Long getMasterOffset(int shard);

    void setIndexedOffset(int shard, long indexOffset);

    Long getIndexedOffset(int shard);

    void setConsumeQueueOffset(String topic, int queueId, int shard, long queueOffset);

    Long getConsumeQueueOffset(String topic, int queueId, int shard);

    void setQueuedOffset(String topic, int queueId, int shard, long commitOffset);

    Long getQueuedOffset(String topic, int queueId, int shard);
}
