package com.wolfmq.common.domain.offset.checkpoint;

import cn.coderule.common.util.lang.collection.MapUtil;
import com.wolfmq.common.domain.offset.checkpoint.store.CheckpointStore;
import java.util.Map;

public class Checkpoint implements CheckpointService {
    private final CheckpointStore store;

    public Checkpoint(CheckpointStore store) {
        this.store = store;
    }

    @Override
    public void updateCommitOffset(int shardId, long commitOffset) {
        store.updateCommitOffset(shardId, commitOffset);
    }

    @Override
    public Long getCommitOffset(int shardId) {
        return store.getCommitOffset(shardId);
    }

    @Override
    public void updateDispatchedOffset(int shardId, long commitOffset) {
        store.updateDispatchedOffset(shardId, commitOffset);
    }

    @Override
    public Long getDispatchedOffset(int shardId) {
        return store.getDispatchedOffset(shardId);
    }

    @Override
    public void updateMasterOffset(int shardId, long commitOffset) {
        store.updateMasterOffset(shardId, commitOffset);
    }

    @Override
    public Long getMasterOffset(int shardId) {
        return store.getMasterOffset(shardId);
    }

    @Override
    public void updateMasterOffsets(Map<Integer, Long> offsetMap) {
        if (MapUtil.isEmpty(offsetMap)) {
            return;
        }

        store.updateMasterOffsets(offsetMap);
    }

    @Override
    public void updateIndexedOffset(int shardId, long indexOffset) {
        store.updateIndexedOffset(shardId, indexOffset);
    }

    @Override
    public Long getIndexedOffset(int shardId) {
        return store.getIndexedOffset(shardId);
    }

    @Override
    public void updateQueueOffset(String topic, int queueId, long queueOffset) {
        store.updateQueueOffset(topic, queueId, queueOffset);
    }

    @Override
    public Long getQueueOffset(String topic, int queueId) {
        return store.getQueueOffset(topic, queueId);
    }

    @Override
    public void updateQueueCommitOffset(String topic, int queueId, int shardId, long commitOffset) {
        store.updateQueueCommitOffset(topic, queueId, shardId, commitOffset);
    }

    @Override
    public Long getQueueCommitOffset(String topic, int queueId, int shardId) {
        return store.getQueueCommitOffset(topic, queueId, shardId);
    }
}
