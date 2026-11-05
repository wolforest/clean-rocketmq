package com.wolfmq.common.server.store.checkpoint.store;

import com.wolfmq.common.server.store.checkpoint.Checkpoint;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Data;

@Data
public class FileCheckpointStore implements Checkpoint,CheckpointStore {
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

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }
}
