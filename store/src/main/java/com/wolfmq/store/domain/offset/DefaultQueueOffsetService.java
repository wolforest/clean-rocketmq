package com.wolfmq.store.domain.offset;

import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.offset.QueueOffsetService;
import com.wolfmq.common.domain.offset.Checkpoint;
import com.wolfmq.common.domain.offset.checkpoint.store.CheckpointStore;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;

public class DefaultQueueOffsetService extends Checkpoint implements QueueOffsetService {
    private final OffsetCodec offsetCodec;

    public DefaultQueueOffsetService(CheckpointStore store, CommitConfig commitConfig) {
        super(store);

        this.offsetCodec = new OffsetCodec(commitConfig.getMaxShardingNumber());
    }

    @Override
    public void setCommitOffset(long commitOffset) {
        int shardId = offsetCodec.getShardId(commitOffset);
        setCommitOffset(shardId, commitOffset);
    }

    @Override
    public void setDispatchedOffset(long commitOffset) {
        int shardId = offsetCodec.getShardId(commitOffset);
        setDispatchedOffset(shardId, commitOffset);
    }

    @Override
    public void setIndexedOffset(long commitOffset) {
        int shardId = offsetCodec.getShardId(commitOffset);
        setIndexedOffset(shardId, commitOffset);
    }

    @Override
    public void setQueueCommitOffset(String topic, int queueId, long commitOffset) {
        int shardId = offsetCodec.getShardId(commitOffset);
        setQueueCommitOffset(topic, queueId, shardId, commitOffset);
    }

    @Override
    public boolean reachesQueueCommitOffset(String topic, int queueId, long commitOffset) {
        int shardId = offsetCodec.getShardId(commitOffset);
        Long oldOffset = getQueueCommitOffset(topic, queueId, shardId);
        if (oldOffset == null) {
            return true;
        }

        return commitOffset >= oldOffset;
    }

    @Override
    public boolean reachesIndexedOffset(long commitOffset) {
        int shardId = offsetCodec.getShardId(commitOffset);
        Long oldOffset = getIndexedOffset(shardId);
        if (oldOffset == null) {
            return true;
        }

        return commitOffset >= oldOffset;
    }
}
