package com.wolfmq.common.domain.offset.checkpoint;

import java.util.Map;

public interface CheckpointService {
    void setCommitOffset(int shard, long commitOffset);

    Long getCommitOffset(int shard);

    void setDispatchedOffset(int shard, long commitOffset);

    Long getDispatchedOffset(int shard);

    void setMasterOffset(int shard, long commitOffset);

    Long getMasterOffset(int shard);

    void setMasterOffset(Map<Integer, Long> offsetMap);

    void setIndexedOffset(int shard, long indexOffset);

    Long getIndexedOffset(int shard);

    void setQueueOffset(String topic, int queueId, long queueOffset);

    Long getQueueOffset(String topic, int queueId);

    void setQueueCommitOffset(String topic, int queueId, int shard, long commitOffset);

    Long getQueueCommitOffset(String topic, int queueId, int shard);
}
