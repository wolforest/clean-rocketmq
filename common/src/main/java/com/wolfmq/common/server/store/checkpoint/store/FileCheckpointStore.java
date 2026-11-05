package com.wolfmq.common.server.store.checkpoint.store;

import com.wolfmq.common.server.store.checkpoint.CheckpointService;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Data;

@Data
public class FileCheckpointStore implements CheckpointService, CheckpointStore {
    private final ConcurrentSkipListMap<Integer, Long> masterOffset = new ConcurrentSkipListMap<>();

    private final ConcurrentSkipListMap<Integer, Long> maxCommitOffset = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<Integer, Long> dispatchedOffset = new ConcurrentSkipListMap<>();

    private final ConcurrentSkipListMap<Integer, Long> indexedOffset = new ConcurrentSkipListMap<>();

    // topicName@queueId@shardId -> queueOffset
    private final ConcurrentSkipListMap<String, Long> queuedOffset = new ConcurrentSkipListMap<>();

    // topicName@queueId -> queueOffset
    private final ConcurrentSkipListMap<String, Long> maxQueueOffset = new ConcurrentSkipListMap<>();
    // topicName@queueId -> queueOffset
    private final ConcurrentSkipListMap<String, Long> minQueueOffset = new ConcurrentSkipListMap<>();

    @Override
    public void setCommitOffset(int shard, long commitOffset) {

    }

    @Override
    public Long getCommitOffset(int shard) {
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
    public void setMasterOffset(int shard, long commitOffset) {

    }

    @Override
    public Long getMasterOffset(int shard) {
        return 0L;
    }

    @Override
    public void setIndexedOffset(int shard, long indexOffset) {

    }

    @Override
    public Long getIndexedOffset(int shard) {
        return 0L;
    }

    @Override
    public void setConsumeQueueOffset(String topic, int queueId, int shard, long queueOffset) {

    }

    @Override
    public Long getConsumeQueueOffset(String topic, int queueId, int shard) {
        return 0L;
    }

    @Override
    public void setQueuedOffset(String topic, int queueId, int shard, long commitOffset) {

    }

    @Override
    public Long getQueuedOffset(String topic, int queueId, int shard) {
        return 0L;
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }
}
