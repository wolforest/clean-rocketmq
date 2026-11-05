package com.wolfmq.common.domain.offset.checkpoint.bakeup.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DispatcherCheckpoint extends Checkpoint implements Serializable {
    // shardId -> commitOffset
    private final ConcurrentSkipListMap<Integer, Long> offsetMap = new ConcurrentSkipListMap<>();
}
