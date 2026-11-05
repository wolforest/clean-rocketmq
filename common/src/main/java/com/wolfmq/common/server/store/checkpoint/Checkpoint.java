package com.wolfmq.common.server.store.checkpoint;

import com.wolfmq.common.server.store.checkpoint.store.CheckpointStore;

public class Checkpoint implements CheckpointService {
    private final CheckpointStore store;

    public Checkpoint(CheckpointStore store) {
        this.store = store;
    }

    @Override
    public void setCommitOffset(int shard, long commitOffset) {

    }

    @Override
    public Long getCommitOffset(int shard) {
        return 0L;
    }

    @Override
    public void setDispatchedOffset(int shard, long commitOffset) {

    }

    @Override
    public Long getDispatchedOffset(int shard) {
        return 0L;
    }

    @Override
    public void setMasterOffset(int shard, long commitOffset) {

    }

    @Override
    public Long getMasterOffset(int shard) {
        return 0L;
    }

    @Override
    public void setIndexedOffset(int shard, long indexOffset) {

    }

    @Override
    public Long getIndexedOffset(int shard) {
        return 0L;
    }

    @Override
    public void setConsumeQueueOffset(String topic, int queueId, int shard, long queueOffset) {

    }

    @Override
    public Long getConsumeQueueOffset(String topic, int queueId, int shard) {
        return 0L;
    }


    @Override
    public void setQueuedOffset(String topic, int queueId, int shard, long commitOffset) {

    }

    @Override
    public Long getQueuedOffset(String topic, int queueId, int shard) {
        return 0L;
    }
}
