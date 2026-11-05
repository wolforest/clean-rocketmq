package com.wolfmq.common.server.store.checkpoint;

public interface Checkpoint {
    void setMaxCommitOffset(int shard, long commitOffset);

    Long getMaxCommitOffset(int shard);

    void setMinCommitOffset(int shard, long commitOffset);

    Long getMinCommitOffset(int shard);

    void setDispatchedOffset(int shard, long commitOffset);

    Long getDispatchedOffset(int shard);

    void setMaxIndexOffset(int shard, long indexOffset);

    Long getMaxIndexOffset(int shard);

    Long getMinQueueOffset(String topic, int queueId, int shard);

    void setMaxQueueOffset(String topic, int queueId, int shard, long queueOffset);

    Long getMaxQueueOffset(String topic, int queueId, int shard);

    void setMinCommittedQueueOffset(String topic, int queueId, int shard, long commitOffset);

    Long getMinCommittedQueueOffset(String topic, int queueId, int shard);

    void setMaxCommittedQueueOffset(String topic, int queueId, int shard, long commitOffset);

    Long getMaxCommittedQueueOffset(String topic, int queueId, int shard);
}
