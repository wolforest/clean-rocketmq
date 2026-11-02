package com.wolfmq.common.server.store.api.meta;

import com.wolfmq.common.domain.consumer.ack.AckInfo;
import com.wolfmq.common.domain.consumer.consume.pop.checkpoint.PopCheckPoint;

public interface AckStore {
    void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset);
    void ack(AckInfo ackInfo, int reviveQueueId, long invisibleTime);
    long getLatestOffset(String topic, String group, int queueId);
}
