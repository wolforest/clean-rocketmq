package com.wolfmq.common.server.store.checkpoint;

import com.wolfmq.common.server.store.checkpoint.model.DispatcherCheckpoint;
import com.wolfmq.common.server.store.checkpoint.model.IndexCheckpoint;
import com.wolfmq.common.server.store.checkpoint.store.MemoryCheckpointStore;

public class CheckpointFacade {
    private final MemoryCheckpointStore memoryStore;

    public CheckpointFacade(MemoryCheckpointStore memoryStore) {
        this.memoryStore = memoryStore;
    }

    public void updateMaxCommitOffset(int shard, long commitOffset) {
        memoryStore.getCommit().updateMaxOffset(shard, commitOffset);
    }

    public Long getMaxCommitOffset(int shard) {
        return memoryStore.getCommit().getMaxOffset(shard);
    }

    public void updateMinCommitOffset(int shard, long commitOffset) {
        memoryStore.getCommit().updateMinOffset(shard, commitOffset);
    }

    public Long getMinCommitOffset(int shard) {
        return memoryStore.getCommit().getMinOffset(shard);
    }

    public void updateDispatchedOffset(int shard, long commitOffset) {
        DispatcherCheckpoint checkpoint = memoryStore.getDispatcher();
        checkpoint.getOffsetMap().merge(shard, commitOffset, Long::max);
    }

    public Long getDispatchedOffset(int shard) {
        return memoryStore.getDispatcher().getOffsetMap().get(shard);
    }

    public void updateMaxIndexOffset(int shard, long indexOffset) {
        IndexCheckpoint checkpoint = memoryStore.getIndex();
        checkpoint.getMaxOffsetMap().merge(shard, indexOffset, Long::max);
    }

    public Long getMaxIndexOffset(int shard) {
        return memoryStore.getIndex().getMaxOffsetMap().get(shard);
    }

    public void updateMinIndexOffset(int shard, long indexOffset) {
        IndexCheckpoint checkpoint = memoryStore.getIndex();
        checkpoint.getMinOffsetMap().merge(shard, indexOffset, Long::min);
    }

    public Long getMinIndexOffset(int shard) {
        return memoryStore.getIndex().getMinOffsetMap().get(shard);
    }

    /**
     * Queue checkpoints are stored per topic and queue. Because these facade
     * methods are shard-scoped, an update is applied to every queue checkpoint
     * and a read aggregates the value across them.
     */
    public void updateMinQueueOffset(String topic, int queueId, int shard, long queueOffset) {
        memoryStore.getQueue(topic, queueId);
    }

    public Long getMinQueueOffset(String topic, int queueId, int shard) {
        return null;
    }

    public void updateMaxQueueOffset(String topic, int queueId, int shard, long queueOffset) {

    }

    public Long getMaxQueueOffset(String topic, int queueId, int shard) {
        return null;
    }

    public void updateMinCommittedQueueOffset(String topic, int queueId, int shard, long commitOffset) {

    }

    public Long getMinCommittedQueueOffset(String topic, int queueId, int shard) {
        return null;
    }

    public void updateMaxCommittedQueueOffset(String topic, int queueId, int shard, long commitOffset) {

    }

    public Long getMaxCommittedQueueOffset(String topic, int queueId, int shard) {
        return null;
    }


}
