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
    public void setMasterOffset(Map<Integer, Long> offsetMap) {
        if (MapUtil.isEmpty(offsetMap)) {
            return;
        }

        store.setMasterOffset(offsetMap);
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
    public void setQueueOffset(String topic, int queueId, int shard, long queueOffset) {
        store.setQueueOffset(topic, queueId, shard, queueOffset);
    }

    @Override
    public Long getQueueOffset(String topic, int queueId, int shard) {
        return store.getQueueOffset(topic, queueId, shard);
    }

    @Override
    public void setQueueCommitOffset(String topic, int queueId, int shard, long commitOffset) {
        store.setQueueCommitOffset(topic, queueId, shard, commitOffset);
    }

    @Override
    public Long getQueueCommitOffset(String topic, int queueId, int shard) {
        return store.getQueueCommitOffset(topic, queueId, shard);
    }
}
