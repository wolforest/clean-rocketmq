package cn.coderule.wolfmq.broker.infra.store;

import cn.coderule.wolfmq.broker.infra.embed.EmbedAckStore;
import cn.coderule.wolfmq.broker.infra.remote.RemoteAckStore;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.wolfmq.domain.domain.store.api.meta.AckStore;

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
