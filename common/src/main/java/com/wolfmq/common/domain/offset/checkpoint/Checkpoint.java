package com.wolfmq.common.domain.offset.checkpoint;

import com.wolfmq.common.domain.offset.checkpoint.store.CheckpointStore;

public class Checkpoint implements CheckpointService {
    private final CheckpointStore store;

    public Checkpoint(CheckpointStore store) {
        this.store = store;
    }

    @Override
    public void setCommitOffset(int shard, long commitOffset) {
        store.setCommitOffset(shard, commitOffset);
    }

    @Override
    public Long getCommitOffset(int shard) {
        return store.getCommitOffset(shard);
    }

    @Override
    public void setDispatchedOffset(int shard, long commitOffset) {
        store.setDispatchedOffset(shard, commitOffset);
    }

    @Override
    public Long getDispatchedOffset(int shard) {
        return store.getDispatchedOffset(shard);
    }

    @Override
    public void setMasterOffset(int shard, long commitOffset) {
        store.setMasterOffset(shard, commitOffset);
    }

    @Override
    public Long getMasterOffset(int shard) {
        return store.getMasterOffset(shard);
    }

    @Override
    public void setIndexedOffset(int shard, long indexOffset) {
        store.setIndexedOffset(shard, indexOffset);
    }

    @Override
    public Long getIndexedOffset(int shard) {
        return store.getIndexedOffset(shard);
    }

    @Override
    public void setConsumeQueueOffset(String topic, int queueId, int shard, long queueOffset) {
        store.setConsumeQueueOffset(topic, queueId, shard, queueOffset);
    }

    @Override
    public Long getConsumeQueueOffset(String topic, int queueId, int shard) {
        return store.getConsumeQueueOffset(topic, queueId, shard);
    }

    @Override
    public void setQueuedOffset(String topic, int queueId, int shard, long commitOffset) {
        store.setQueuedOffset(topic, queueId, shard, commitOffset);
    }

    @Override
    public Long getQueuedOffset(String topic, int queueId, int shard) {
        return store.getQueuedOffset(topic, queueId, shard);
    }
}
