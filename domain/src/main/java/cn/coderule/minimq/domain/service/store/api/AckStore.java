package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.domain.model.consumer.pop.ack.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;

public interface AckStore {
    void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset);
    void ack(AckMsg ackMsg, int reviveQueueId, long invisibleTime);
    long getLatestOffset(String topic, String group, int queueId);
}
