package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedSubscriptionStore;
import cn.coderule.minimq.broker.infra.remote.RemoteSubscriptionStore;
import cn.coderule.minimq.domain.config.BrokerConfig;

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

}
