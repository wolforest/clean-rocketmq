package com.wolfmq.common.domain.offset;

import com.wolfmq.common.domain.offset.checkpoint.CheckpointService;

/**
 * @renamed from QueueOffsetService to QueueProgressService
 * @renamed from QueueProgressService to QueueProgress
 */
public interface QueueProgress extends CheckpointService {
    boolean isShutdownSuccessfully();
    void setShutdownSuccessfully(boolean shutdownSuccessfully);

    void updateCommitOffset(long commitOffset);
    void updateDispatchedOffset(long commitOffset);
    void updateIndexedOffset(long commitOffset);
    void updateQueueCommitOffset(String topic, int queueId, long commitOffset);

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

    long assignQueueOffset(String topic, int queueId, int num);

    default long assignQueueOffset(String topic, int queueId) {
        return assignQueueOffset(topic, queueId, 1);
    }
}
