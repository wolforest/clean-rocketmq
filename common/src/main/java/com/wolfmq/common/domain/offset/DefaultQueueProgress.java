package com.wolfmq.common.domain.offset;

import com.wolfmq.common.domain.offset.checkpoint.CheckpointFacade;
import com.wolfmq.common.domain.offset.checkpoint.store.CheckpointStore;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import com.wolfmq.common.domain.offset.consumequeue.QueueOffsetAssigner;

public class DefaultQueueProgress extends CheckpointFacade implements QueueProgress {
    private final OffsetCodec offsetCodec;
    private final QueueOffsetAssigner queueOffsetAssigner;
    private boolean shutdownSuccessfully = false;

    public DefaultQueueProgress(
        CheckpointStore store,
        OffsetCodec offsetCodec,
        QueueOffsetAssigner queueOffsetAssigner
    ) {
        super(store);

        this.offsetCodec = offsetCodec;
        this.queueOffsetAssigner = queueOffsetAssigner;
    }

    @Override
    public boolean isShutdownSuccessfully() {
        return shutdownSuccessfully;
    }

    @Override
    public void setShutdownSuccessfully(boolean shutdownSuccessfully) {
        this.shutdownSuccessfully = shutdownSuccessfully;
    }

    @Override
    public void updateCommitOffset(long commitOffset) {
        int shardId = offsetCodec.shardIdOf(commitOffset);
        updateCommitOffset(shardId, commitOffset);
    }

    @Override
    public void updateDispatchedOffset(long commitOffset) {
        int shardId = offsetCodec.shardIdOf(commitOffset);
        updateDispatchedOffset(shardId, commitOffset);
    }

    @Override
    public void updateIndexedOffset(long commitOffset) {
        int shardId = offsetCodec.shardIdOf(commitOffset);
        updateIndexedOffset(shardId, commitOffset);
    }

    @Override
    public void updateQueueCommitOffset(String topic, int queueId, long commitOffset) {
        int shardId = offsetCodec.shardIdOf(commitOffset);
        updateQueueCommitOffset(topic, queueId, shardId, commitOffset);
    }

    @Override
    public boolean reachesQueueCommitOffset(String topic, int queueId, long commitOffset) {
        int shardId = offsetCodec.shardIdOf(commitOffset);
        Long oldOffset = getQueueCommitOffset(topic, queueId, shardId);
        if (oldOffset == null) {
            return true;
        }

        return commitOffset >= oldOffset;
    }

    @Override
    public boolean reachesIndexedOffset(long commitOffset) {
        int shardId = offsetCodec.shardIdOf(commitOffset);
        Long oldOffset = getIndexedOffset(shardId);
        if (oldOffset == null) {
            return true;
        }

        return commitOffset >= oldOffset;
    }

    @Override
    public long assignQueueOffset(String topic, int queueId, int num) {
        return queueOffsetAssigner.assign(topic, queueId, num);
    }

    @Override
    public void updateQueueOffset(String topic, int queueId, long queueOffset) {
        queueOffsetAssigner.updateQueueOffset(topic, queueId, queueOffset);
    }

    @Override
    public Long getQueueOffset(String topic, int queueId) {
        return queueOffsetAssigner.getQueueOffset(topic, queueId);
    }
}
