package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPoint;

public interface AckStore {
    void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset);
    void ack(PopCheckPoint point, int reviveQueueId);
}
