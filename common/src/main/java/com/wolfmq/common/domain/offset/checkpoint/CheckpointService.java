package com.wolfmq.common.domain.offset.checkpoint;

import java.util.Map;

public interface CheckpointService {
    Map<String, Long> getQueueOffsetMap();

    void updateCommitOffset(int shardId, long commitOffset);

    Long getCommitOffset(int shardId);

    void updateDispatchedOffset(int shardId, long commitOffset);

    Long getDispatchedOffset(int shardId);

    void updateMasterOffset(int shardId, long commitOffset);

    Long getMasterOffset(int shardId);

    void updateMasterOffsets(Map<Integer, Long> offsetMap);

    void updateIndexedOffset(int shardId, long indexOffset);

    Long getIndexedOffset(int shardId);

    void updateQueueOffset(String topic, int queueId, long queueOffset);

    Long getQueueOffset(String topic, int queueId);

    void updateQueueCommitOffset(String topic, int queueId, int shardId, long commitOffset);

    Long getQueueCommitOffset(String topic, int queueId, int shardId);
}
