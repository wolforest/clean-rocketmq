package com.wolfmq.common.domain.offset.checkpoint.store;

import com.wolfmq.common.domain.offset.checkpoint.CheckpointService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;

@Data
public class MemoryCheckpointStore implements CheckpointService {
    private static final String KEY_SEPARATOR = "@";

    private final ConcurrentMap<Integer, Long> masterOffsetMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Long> commitOffsetMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Long> dispatchedOffsetMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Long> indexedOffsetMap = new ConcurrentHashMap<>();

    // topicName@queueId@shardId -> commitOffset
    private final ConcurrentMap<String, Long> queueCommitOffsetMap = new ConcurrentHashMap<>();

    // topicName@queueId -> queueOffset
    private final ConcurrentMap<String, Long> queueOffsetMap = new ConcurrentHashMap<>();

    @Override
    public void updateCommitOffset(int shardId, long commitOffset) {
        commitOffsetMap.merge(shardId, commitOffset, Math::max);
    }

    @Override
    public Long getCommitOffset(int shardId) {
        return commitOffsetMap.get(shardId);
    }

    @Override
    public void updateDispatchedOffset(int shardId, long commitOffset) {
        dispatchedOffsetMap.merge(shardId, commitOffset, Math::max);
    }

    @Override
    public Long getDispatchedOffset(int shardId) {
        return dispatchedOffsetMap.get(shardId);
    }

    @Override
    public void updateMasterOffset(int shardId, long commitOffset) {
        masterOffsetMap.merge(shardId, commitOffset, Math::max);
    }

    @Override
    public Long getMasterOffset(int shardId) {
        return masterOffsetMap.get(shardId);
    }

    @Override
    public void updateMasterOffsets(Map<Integer, Long> offsetMap) {
        masterOffsetMap.putAll(offsetMap);
    }

    @Override
    public void updateIndexedOffset(int shardId, long indexOffset) {
        indexedOffsetMap.merge(shardId, indexOffset, Math::max);
    }

    @Override
    public Long getIndexedOffset(int shardId) {
        return indexedOffsetMap.get(shardId);
    }

    @Override
    public void updateQueueOffset(String topic, int queueId, long queueOffset) {
        queueOffsetMap.merge(
            buildQueueKey(topic, queueId),
            queueOffset,
            Math::max
        );
    }

    @Override
    public Long getQueueOffset(String topic, int queueId) {
        return queueOffsetMap.get(
            buildQueueKey(topic, queueId)
        );
    }

    @Override
    public void updateQueueCommitOffset(String topic, int queueId, int shardId, long commitOffset) {
        queueCommitOffsetMap.merge(
            buildQueueShardKey(topic, queueId, shardId),
            commitOffset,
            Math::max
        );
    }

    @Override
    public Long getQueueCommitOffset(String topic, int queueId, int shardId) {
        return queueCommitOffsetMap.get(
            buildQueueShardKey(topic, queueId, shardId)
        );
    }

    private String buildQueueKey(String topic, int queueId) {
        return topic
            + KEY_SEPARATOR
            + pad2(queueId);
    }

    private String buildQueueShardKey(String topic, int queueId, int shardId) {
        return buildQueueKey(topic, queueId)
            + KEY_SEPARATOR
            + pad2(shardId);
    }

    private String pad2(int value) {
        return String.format("%02d", value);
    }
}
