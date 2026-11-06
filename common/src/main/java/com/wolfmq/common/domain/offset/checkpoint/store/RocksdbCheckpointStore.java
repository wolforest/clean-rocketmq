package com.wolfmq.common.domain.offset.checkpoint.store;

import com.wolfmq.common.domain.offset.checkpoint.CheckpointService;
import java.util.Map;
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
    public void setMasterOffset(Map<Integer, Long> offsetMap) {
        memoryStore.setMasterOffset(offsetMap);
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
    public void setQueueOffset(String topic, int queueId, int shard, long queueOffset) {
        memoryStore.setQueueOffset(topic, queueId, shard, queueOffset);
    }

    @Override
    public Long getQueueOffset(String topic, int queueId, int shard) {
        return memoryStore.getQueueOffset(topic, queueId, shard);
    }

    @Override
    public void setQueueCommitOffset(String topic, int queueId, int shard, long commitOffset) {
        memoryStore.setQueueCommitOffset(topic, queueId, shard, commitOffset);
    }

    @Override
    public Long getQueueCommitOffset(String topic, int queueId, int shard) {
        return memoryStore.getQueueCommitOffset(topic, queueId, shard);
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }
}
