package com.wolfmq.common.domain.offset.checkpoint.store;

import com.wolfmq.common.domain.offset.checkpoint.CheckpointService;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Data;

@Data
public class MemoryCheckpointStore implements CheckpointService {
    private static final String KEY_SEPARATOR = "@";

    private final ConcurrentSkipListMap<Integer, Long> masterOffsetMap = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<Integer, Long> commitOffsetMap = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<Integer, Long> dispatchedOffsetMap = new ConcurrentSkipListMap<>();
    private final ConcurrentSkipListMap<Integer, Long> indexedOffsetMap = new ConcurrentSkipListMap<>();

    // topicName@queueId@shardId -> commitOffset
    private final ConcurrentSkipListMap<String, Long> queueCommitOffsetMap = new ConcurrentSkipListMap<>();

    // topicName@queueId -> queueOffset
    private final ConcurrentSkipListMap<String, Long> queueOffsetMap = new ConcurrentSkipListMap<>();

    @Override
    public void setCommitOffset(int shard, long commitOffset) {
        commitOffsetMap.merge(shard, commitOffset, Math::max);
    }

    @Override
    public Long getCommitOffset(int shard) {
        return commitOffsetMap.get(shard);
    }

    @Override
    public void setDispatchedOffset(int shard, long commitOffset) {
        dispatchedOffsetMap.merge(shard, commitOffset, Math::max);
    }

    @Override
    public Long getDispatchedOffset(int shard) {
        return dispatchedOffsetMap.get(shard);
    }

    @Override
    public void setMasterOffset(int shard, long commitOffset) {
        masterOffsetMap.merge(shard, commitOffset, Math::max);
    }

    @Override
    public Long getMasterOffset(int shard) {
        return masterOffsetMap.get(shard);
    }

    @Override
    public void setMasterOffset(Map<Integer, Long> offsetMap) {
        masterOffsetMap.putAll(offsetMap);
    }

    @Override
    public void setIndexedOffset(int shard, long indexOffset) {
        indexedOffsetMap.merge(shard, indexOffset, Math::max);
    }

    @Override
    public Long getIndexedOffset(int shard) {
        return indexedOffsetMap.get(shard);
    }

    @Override
    public void setQueueOffset(String topic, int queueId, int shard, long queueOffset) {
        queueOffsetMap.merge(
            buildKey(topic, queueId),
            queueOffset,
            Math::max
        );
    }

    @Override
    public Long getQueueOffset(String topic, int queueId, int shard) {
        return queueOffsetMap.get(
            buildKey(topic, queueId)
        );
    }

    @Override
    public void setQueueCommitOffset(String topic, int queueId, int shard, long commitOffset) {
        queueCommitOffsetMap.merge(
            buildKey(topic, queueId, shard),
            commitOffset,
            Math::max
        );
    }

    @Override
    public Long getQueueCommitOffset(String topic, int queueId, int shard) {
        return queueCommitOffsetMap.get(
            buildKey(topic, queueId, shard)
        );
    }

    private String buildKey(String topic, int queueId) {
        return topic
            + KEY_SEPARATOR
            + pad2(queueId);
    }

    private String buildKey(String topic, int queueId, int shard) {
        return buildKey(topic, queueId)
            + KEY_SEPARATOR
            + pad2(shard);
    }

    private String pad2(int value) {
        return String.format("%02d", value);
    }
}
