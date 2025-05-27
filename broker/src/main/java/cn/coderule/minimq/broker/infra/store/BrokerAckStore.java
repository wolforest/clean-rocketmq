package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedAckStore;
import cn.coderule.minimq.broker.infra.remote.RemoteAckStore;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.consumer.pop.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPoint;
import cn.coderule.minimq.domain.service.store.api.AckStore;

public class BrokerAckStore implements AckStore {
    private final BrokerConfig brokerConfig;
    private final EmbedAckStore embedAckStore;
    private final RemoteAckStore remoteAckStore;

    public BrokerAckStore(BrokerConfig brokerConfig, EmbedAckStore embedAckStore, RemoteAckStore remoteAckStore) {
        this.brokerConfig = brokerConfig;
        this.embedAckStore = embedAckStore;
        this.remoteAckStore = remoteAckStore;
    }

    @Override
    public void addCheckPoint(PopCheckPoint point, int reviveQueueId, long reviveQueueOffset, long nextBeginOffset) {
        if (embedAckStore.isEmbed(point.getTopic())) {
            embedAckStore.addCheckPoint(point, reviveQueueId, reviveQueueOffset, nextBeginOffset);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteAckStore.addCheckPoint(point, reviveQueueId, reviveQueueOffset, nextBeginOffset);
    }

    @Override
    public void ack(AckMsg ackMsg, int reviveQueueId, long invisibleTime) {
        if (embedAckStore.isEmbed(ackMsg.getTopic())) {
            embedAckStore.ack(ackMsg, reviveQueueId, invisibleTime);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteAckStore.ack(ackMsg, reviveQueueId, invisibleTime);
    }

    @Override
    public long getLatestOffset(String topic, String group, int queueId) {
        if (embedAckStore.isEmbed(topic)) {
            return embedAckStore.getLatestOffset(topic, group, queueId);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return -1;
        }

        return remoteAckStore.getLatestOffset(topic, group, queueId);
    }
}
