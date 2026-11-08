package com.wolfmq.common.domain.offset.checkpoint.store;

import com.wolfmq.common.domain.offset.checkpoint.CheckpointService;
import java.util.Map;
import lombok.Data;

@Data
public class RocksdbCheckpointStore implements CheckpointService, CheckpointStore {
    private MemoryCheckpointStore memoryStore = new MemoryCheckpointStore();

    @Override
    public void setCommitOffset(int shardId, long commitOffset) {
        memoryStore.setCommitOffset(shardId, commitOffset);
    }

    @Override
    public Long getCommitOffset(int shardId) {
        return memoryStore.getCommitOffset(shardId);
    }

    @Override
    public void setDispatchedOffset(int shardId, long commitOffset) {
        memoryStore.setDispatchedOffset(shardId, commitOffset);
    }

    @Override
    public Long getDispatchedOffset(int shardId) {
        return memoryStore.getDispatchedOffset(shardId);
    }

    @Override
    public void setMasterOffset(int shardId, long commitOffset) {
        memoryStore.setMasterOffset(shardId, commitOffset);
    }

    @Override
    public Long getMasterOffset(int shardId) {
        return memoryStore.getMasterOffset(shardId);
    }

    @Override
    public void setMasterOffsets(Map<Integer, Long> offsetMap) {
        memoryStore.setMasterOffsets(offsetMap);
    }

    @Override
    public void setIndexedOffset(int shardId, long indexOffset) {
        memoryStore.setIndexedOffset(shardId, indexOffset);
    }

    @Override
    public Long getIndexedOffset(int shardId) {
        return memoryStore.getIndexedOffset(shardId);
    }

    @Override
    public void setQueueOffset(String topic, int queueId, long queueOffset) {
        memoryStore.setQueueOffset(topic, queueId, queueOffset);
    }

    @Override
    public Long getQueueOffset(String topic, int queueId) {
        return memoryStore.getQueueOffset(topic, queueId);
    }

    @Override
    public void setQueueCommitOffset(String topic, int queueId, int shardId, long commitOffset) {
        memoryStore.setQueueCommitOffset(topic, queueId, shardId, commitOffset);
    }

    @Override
    public Long getQueueCommitOffset(String topic, int queueId, int shardId) {
        return memoryStore.getQueueCommitOffset(topic, queueId, shardId);
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }
}
