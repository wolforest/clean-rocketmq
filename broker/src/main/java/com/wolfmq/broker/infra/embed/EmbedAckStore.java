package com.wolfmq.broker.infra.embed;

import com.wolfmq.domain.domain.consumer.ack.AckInfo;
import com.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import com.wolfmq.domain.server.store.api.meta.AckStore;

@Deprecated
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
    public void ack(AckInfo ackInfo, int reviveQueueId, long invisibleTime) {
        ackStore.ack(ackInfo, reviveQueueId, invisibleTime);
    }

    @Override
    public long getLatestOffset(String topic, String group, int queueId) {
        return ackStore.getLatestOffset(topic, group, queueId);
    }
}
