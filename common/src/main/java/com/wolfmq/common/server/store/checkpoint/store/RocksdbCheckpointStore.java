package com.wolfmq.common.server.store.checkpoint.store;

import com.wolfmq.common.server.store.checkpoint.CheckpointService;
import lombok.Data;

@Data
public class RocksdbCheckpointStore implements CheckpointService, CheckpointStore {
    private MemoryCheckpointStore memoryStore = new MemoryCheckpointStore();

    @Override
    public void setCommitOffset(int shard, long commitOffset) {
        memoryStore.setCommitOffset(shard, commitOffset);
    }

    @Override
    public Long getCommitOffset(int shard) {
        return memoryStore.getCommitOffset(shard);
    }

    @Override
    public void setDispatchedOffset(int shard, long commitOffset) {
        memoryStore.setDispatchedOffset(shard, commitOffset);
    }

    @Override
    public Long getDispatchedOffset(int shard) {
        return memoryStore.getDispatchedOffset(shard);
    }

    @Override
    public void setMasterOffset(int shard, long commitOffset) {
        memoryStore.setMasterOffset(shard, commitOffset);
    }

    @Override
    public Long getMasterOffset(int shard) {
        return memoryStore.getMasterOffset(shard);
    }

    @Override
    public void setIndexedOffset(int shard, long indexOffset) {
        memoryStore.setIndexedOffset(shard, indexOffset);
    }

    @Override
    public Long getIndexedOffset(int shard) {
        return memoryStore.getIndexedOffset(shard);
    }

    @Override
    public void setConsumeQueueOffset(String topic, int queueId, int shard, long queueOffset) {
        memoryStore.setConsumeQueueOffset(topic, queueId, shard, queueOffset);
    }

    @Override
    public Long getConsumeQueueOffset(String topic, int queueId, int shard) {
        return memoryStore.getConsumeQueueOffset(topic, queueId, shard);
    }

    @Override
    public void setQueuedOffset(String topic, int queueId, int shard, long commitOffset) {
        memoryStore.setQueuedOffset(topic, queueId, shard, commitOffset);
    }

    @Override
    public Long getQueuedOffset(String topic, int queueId, int shard) {
        return memoryStore.getQueuedOffset(topic, queueId, shard);
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }
}
