package com.wolfmq.common.server.store.checkpoint.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class IndexCheckpoint extends Checkpoint implements Serializable {
    // shardId -> commitOffset
    private final ConcurrentSkipListMap<Integer, Long> maxOffsetMap = new ConcurrentSkipListMap<>();
}
