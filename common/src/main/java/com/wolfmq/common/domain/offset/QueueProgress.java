package com.wolfmq.common.domain.offset;

import com.wolfmq.common.domain.offset.checkpoint.CheckpointService;

/**
 * @renamed from QueueOffsetService to QueueProgressService
 * @renamed from QueueProgressService to QueueProgress
 */
public interface QueueProgress extends CheckpointService {
    void setCommitOffset(long commitOffset);
    void setDispatchedOffset(long commitOffset);
    void setIndexedOffset(long commitOffset);
    void setQueueCommitOffset(String topic, int queueId, long commitOffset);

    /**
     * Compares the given queueCommitOffset with the queue commit offset in the checkpoint.
     *
     * @return true if the given offset is greater than
     *              or equal to the checkpoint offset
     *              or the checkpoint offset is null;
     *         false otherwise.
     */
    boolean reachesQueueCommitOffset(String topic, int queueId, long commitOffset);
    boolean reachesIndexedOffset(long commitOffset);
}
