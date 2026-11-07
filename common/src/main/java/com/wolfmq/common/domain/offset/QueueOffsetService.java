package com.wolfmq.common.domain.offset;

import com.wolfmq.common.domain.offset.checkpoint.CheckpointService;

public interface QueueOffsetService extends CheckpointService {
    void setCommitOffset(long commitOffset);
    void setDispatchedOffset(long commitOffset);
    void setIndexedOffset(long commitOffset);
    void setQueueCommitOffset(String topic, int queueId, long commitOffset);
    boolean exceedsQueueCommitOffset(String topic, int queueId, long commitOffset);
    boolean exceedsIndexedOffset(long commitOffset);
}
