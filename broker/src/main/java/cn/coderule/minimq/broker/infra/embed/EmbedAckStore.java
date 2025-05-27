package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.model.consumer.pop.ack.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.service.store.api.AckStore;

public class EmbedAckStore extends AbstractEmbedStore implements AckStore {
    private final AckStore ackStore;

    public EmbedAckStore(AckStore ackStore, EmbedLoadBalance loadBalance) {
        super(loadBalance);
        this.ackStore = ackStore;
    }

    @Override
    public void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset) {
        ackStore.addCheckPoint(point, reviveQueueId, reviveQueueOffset, nextBeginOffset);
    }

    @Override
    public void ack(AckMsg ackMsg, int reviveQueueId, long invisibleTime) {
        ackStore.ack(ackMsg, reviveQueueId, invisibleTime);
    }

    @Override
    public long getLatestOffset(String topic, String group, int queueId) {
        return ackStore.getLatestOffset(topic, group, queueId);
    }
}
