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
    public void setCommitOffset(int shardId, long commitOffset) {
        store.setCommitOffset(shardId, commitOffset);
    }

    @Override
    public Long getCommitOffset(int shardId) {
        return store.getCommitOffset(shardId);
    }

    @Override
    public void setDispatchedOffset(int shardId, long commitOffset) {
        store.setDispatchedOffset(shardId, commitOffset);
    }

    @Override
    public Long getDispatchedOffset(int shardId) {
        return store.getDispatchedOffset(shardId);
    }

    @Override
    public void setMasterOffset(int shardId, long commitOffset) {
        store.setMasterOffset(shardId, commitOffset);
    }

    @Override
    public Long getMasterOffset(int shardId) {
        return store.getMasterOffset(shardId);
    }

    @Override
    public void setMasterOffsets(Map<Integer, Long> offsetMap) {
        if (MapUtil.isEmpty(offsetMap)) {
            return;
        }

        store.setMasterOffsets(offsetMap);
    }

    @Override
    public void setIndexedOffset(int shardId, long indexOffset) {
        store.setIndexedOffset(shardId, indexOffset);
    }

    @Override
    public Long getIndexedOffset(int shardId) {
        return store.getIndexedOffset(shardId);
    }

    @Override
    public void setQueueOffset(String topic, int queueId, long queueOffset) {
        store.setQueueOffset(topic, queueId, queueOffset);
    }

    @Override
    public Long getQueueOffset(String topic, int queueId) {
        return store.getQueueOffset(topic, queueId);
    }

    @Override
    public void setQueueCommitOffset(String topic, int queueId, int shardId, long commitOffset) {
        store.setQueueCommitOffset(topic, queueId, shardId, commitOffset);
    }

    @Override
    public Long getQueueCommitOffset(String topic, int queueId, int shardId) {
        return store.getQueueCommitOffset(topic, queueId, shardId);
    }
}
