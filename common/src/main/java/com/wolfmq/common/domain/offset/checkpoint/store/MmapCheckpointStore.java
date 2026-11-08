package com.wolfmq.common.domain.offset.checkpoint.store;

import com.wolfmq.common.domain.offset.checkpoint.CheckpointService;
import java.util.Map;
import lombok.Data;

@Data
public class MmapCheckpointStore implements CheckpointService, CheckpointStore {
    private MemoryCheckpointStore memoryStore = new MemoryCheckpointStore();

    @Override
    public void updateCommitOffset(int shardId, long commitOffset) {
        memoryStore.updateCommitOffset(shardId, commitOffset);
    }

    @Override
    public Long getCommitOffset(int shardId) {
        return memoryStore.getCommitOffset(shardId);
    }

    @Override
    public void updateDispatchedOffset(int shardId, long commitOffset) {
        memoryStore.updateDispatchedOffset(shardId, commitOffset);
    }

    @Override
    public Long getDispatchedOffset(int shardId) {
        return memoryStore.getDispatchedOffset(shardId);
    }

    @Override
    public void updateMasterOffset(int shardId, long commitOffset) {
        memoryStore.updateMasterOffset(shardId, commitOffset);
    }

    @Override
    public Long getMasterOffset(int shardId) {
        return memoryStore.getMasterOffset(shardId);
    }

    @Override
    public void updateMasterOffsets(Map<Integer, Long> offsetMap) {
        memoryStore.updateMasterOffsets(offsetMap);
    }

    @Override
    public void updateIndexedOffset(int shardId, long indexOffset) {
        memoryStore.updateIndexedOffset(shardId, indexOffset);
    }

    @Override
    public Long getIndexedOffset(int shardId) {
        return memoryStore.getIndexedOffset(shardId);
    }

    @Override
    public Map<String, Long> getQueueOffsetMap() {
        return memoryStore.getQueueOffsetMap();
    }

    @Override
    public void updateQueueOffset(String topic, int queueId, long queueOffset) {
        memoryStore.updateQueueOffset(topic, queueId, queueOffset);
    }

    @Override
    public Long getQueueOffset(String topic, int queueId) {
        return memoryStore.getQueueOffset(topic, queueId);
    }

    @Override
    public void updateQueueCommitOffset(String topic, int queueId, int shardId, long commitOffset) {
        memoryStore.updateQueueCommitOffset(topic, queueId, shardId, commitOffset);
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
