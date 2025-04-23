package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedTopicStore;
import cn.coderule.minimq.broker.infra.remote.RemoteTopicStore;
import cn.coderule.minimq.domain.config.BrokerConfig;

public class BrokerTopicStore {
    private final BrokerConfig brokerConfig;
    private final EmbedTopicStore embedTopicStore;
    private final RemoteTopicStore remoteTopicStore;

    public BrokerTopicStore(BrokerConfig brokerConfig, EmbedTopicStore embedTopicStore, RemoteTopicStore remoteTopicStore) {
        this.brokerConfig = brokerConfig;
        this.embedTopicStore = embedTopicStore;
        this.remoteTopicStore = remoteTopicStore;
    }
}
