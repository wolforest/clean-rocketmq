package com.wolfmq.common.server.store.checkpoint;

public class CheckpointFacade implements Checkpoint {

    @Override
    public void setMaxCommitOffset(int shard, long commitOffset) {

    }

    @Override
    public Long getMaxCommitOffset(int shard) {
        return 0L;
    }

    @Override
    public void setMinCommitOffset(int shard, long commitOffset) {

    }

    @Override
    public Long getMinCommitOffset(int shard) {
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
    public void setMaxIndexOffset(int shard, long indexOffset) {

    }

    @Override
    public Long getMaxIndexOffset(int shard) {
        return 0L;
    }

    @Override
    public Long getMinQueueOffset(String topic, int queueId, int shard) {
        return 0L;
    }

    @Override
    public void setMaxQueueOffset(String topic, int queueId, int shard, long queueOffset) {

    }

    @Override
    public Long getMaxQueueOffset(String topic, int queueId, int shard) {
        return 0L;
    }

    @Override
    public void setMinCommittedQueueOffset(String topic, int queueId, int shard, long commitOffset) {

    }

    @Override
    public Long getMinCommittedQueueOffset(String topic, int queueId, int shard) {
        return 0L;
    }

    @Override
    public void setMaxCommittedQueueOffset(String topic, int queueId, int shard, long commitOffset) {

    }

    @Override
    public Long getMaxCommittedQueueOffset(String topic, int queueId, int shard) {
        return 0L;
    }
}
