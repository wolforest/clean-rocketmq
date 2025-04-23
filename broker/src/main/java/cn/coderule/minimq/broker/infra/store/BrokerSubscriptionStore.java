package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedSubscriptionStore;
import cn.coderule.minimq.broker.infra.remote.RemoteSubscriptionStore;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.subscription.SubscriptionGroup;

public class BrokerSubscriptionStore {
    private final BrokerConfig brokerConfig;
    private final EmbedSubscriptionStore embedSubscriptionStore;
    private final RemoteSubscriptionStore remoteSubscriptionStore;

    public BrokerSubscriptionStore(BrokerConfig brokerConfig, EmbedSubscriptionStore embedSubscriptionStore,
        RemoteSubscriptionStore remoteSubscriptionStore) {
        this.brokerConfig = brokerConfig;
        this.embedSubscriptionStore = embedSubscriptionStore;
        this.remoteSubscriptionStore = remoteSubscriptionStore;
    }

    public SubscriptionGroup getGroup(String topicName, String groupName) {
        if (embedSubscriptionStore.existsGroup(groupName)) {
            return embedSubscriptionStore.getGroup(groupName);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return null;
        }

        return remoteSubscriptionStore.getGroup(topicName, groupName);
    }

}
