package com.wolfmq.common.server.store.checkpoint.bakeup.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class QueueCheckpoint extends Checkpoint implements Serializable {

    // topicName@queueId -> queueOffset
    private final ConcurrentSkipListMap<String, Long> maxOffsetMap = new ConcurrentSkipListMap<>();
    // topicName@queueId -> queueOffset
    private final ConcurrentSkipListMap<String, Long> minOffsetMap = new ConcurrentSkipListMap<>();

    /**
     * topicName@queueId@shardId -> commitOffset
     * 20 * 10 * 10     = 2,000
     * 100 * 10 * 10    = 10,000
     */
    private final ConcurrentSkipListMap<String, Long> maxCommitOffsetMap = new ConcurrentSkipListMap<>();
}
