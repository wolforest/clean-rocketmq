package com.wolfmq.common.server.store.checkpoint.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class CommitCheckpoint extends Checkpoint implements Serializable {
    private final ConcurrentSkipListMap<Integer, Long> maxOffsetMap = new ConcurrentSkipListMap<>();

    // useless, to do delete
    private final ConcurrentSkipListMap<Integer, Long> minOffsetMap = new ConcurrentSkipListMap<>();

    public void updateMaxOffset(int shardId, long offset) {
        maxOffsetMap.merge(shardId, offset, Long::max);
        markChanged();
    }

    public Long getMaxOffset(int shardId) {
        return maxOffsetMap.get(shardId);
    }

    public void updateMinOffset(int shardId, long offset) {
        minOffsetMap.merge(shardId, offset, Long::min);
        markChanged();
    }

    public Long getMinOffset(int shardId) {
        return minOffsetMap.get(shardId);
    }
}
