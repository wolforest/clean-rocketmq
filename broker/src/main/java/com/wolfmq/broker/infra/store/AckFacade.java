package com.wolfmq.broker.infra.store;

import com.wolfmq.broker.infra.embed.EmbedAckStore;
import com.wolfmq.broker.infra.remote.RemoteAckStore;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.consumer.ack.AckInfo;
import com.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import com.wolfmq.domain.server.store.api.meta.AckStore;

@Deprecated
public class AckFacade implements AckStore {
    private final BrokerConfig brokerConfig;
    private final EmbedAckStore embedAckStore;
    private final RemoteAckStore remoteAckStore;

    public AckFacade(BrokerConfig brokerConfig, EmbedAckStore embedAckStore, RemoteAckStore remoteAckStore) {
        this.brokerConfig = brokerConfig;
        this.embedAckStore = embedAckStore;
        this.remoteAckStore = remoteAckStore;
    }

    @Override
    public void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset) {
        if (embedAckStore.containsTopic(point.getTopic())) {
            embedAckStore.addCheckPoint(point, reviveQueueId, reviveQueueOffset, nextBeginOffset);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteAckStore.addCheckPoint(point, reviveQueueId, reviveQueueOffset, nextBeginOffset);
    }

    @Override
    public void ack(AckInfo ackInfo, int reviveQueueId, long invisibleTime) {
        if (embedAckStore.containsTopic(ackInfo.getTopic())) {
            embedAckStore.ack(ackInfo, reviveQueueId, invisibleTime);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteAckStore.ack(ackInfo, reviveQueueId, invisibleTime);
    }

    @Override
    public long getLatestOffset(String topic, String group, int queueId) {
        if (embedAckStore.containsTopic(topic)) {
            return embedAckStore.getLatestOffset(topic, group, queueId);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return -1;
        }

        return remoteAckStore.getLatestOffset(topic, group, queueId);
    }
}
