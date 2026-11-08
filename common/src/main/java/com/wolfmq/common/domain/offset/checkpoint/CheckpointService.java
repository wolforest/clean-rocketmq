package com.wolfmq.common.domain.offset.checkpoint;

import java.util.Map;

public interface CheckpointService {
    void setCommitOffset(int shardId, long commitOffset);

    Long getCommitOffset(int shardId);

    void setDispatchedOffset(int shardId, long commitOffset);

    Long getDispatchedOffset(int shardId);

    void setMasterOffset(int shardId, long commitOffset);

    Long getMasterOffset(int shardId);

    void setMasterOffsets(Map<Integer, Long> offsetMap);

    void setIndexedOffset(int shardId, long indexOffset);

    Long getIndexedOffset(int shardId);

    void setQueueOffset(String topic, int queueId, long queueOffset);

    Long getQueueOffset(String topic, int queueId);

    void setQueueCommitOffset(String topic, int queueId, int shardId, long commitOffset);

    Long getQueueCommitOffset(String topic, int queueId, int shardId);
}
