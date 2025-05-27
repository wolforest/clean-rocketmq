package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.model.consumer.pop.ack.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.service.store.api.AckStore;
import cn.coderule.minimq.store.domain.mq.ack.AckService;

public class AckStoreImpl implements AckStore {
    private final AckService ackService;

    public AckStoreImpl(AckService ackService) {
        this.ackService = ackService;
    }

    @Override
    public void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset) {
        ackService.addCheckPoint(point, reviveQueueId, reviveQueueOffset, nextBeginOffset);
    }

    @Override
    public void ack(AckMsg ackMsg, int reviveQueueId, long invisibleTime) {
        ackService.ack(ackMsg, reviveQueueId, invisibleTime);
    }

    @Override
    public long getLatestOffset(String topic, String group, int queueId) {
        return ackService.getLatestOffset(topic, group, queueId);
    }
}
