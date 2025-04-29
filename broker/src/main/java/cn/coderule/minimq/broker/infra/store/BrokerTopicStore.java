package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedTopicStore;
import cn.coderule.minimq.broker.infra.remote.RemoteTopicStore;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.domain.service.broker.infra.TopicStore;
import java.util.concurrent.CompletableFuture;

public class BrokerTopicStore implements TopicStore {
    private final BrokerConfig brokerConfig;
    private final EmbedTopicStore embedTopicStore;
    private final RemoteTopicStore remoteTopicStore;

    public BrokerTopicStore(BrokerConfig brokerConfig, EmbedTopicStore embedTopicStore, RemoteTopicStore remoteTopicStore) {
        this.brokerConfig = brokerConfig;
        this.embedTopicStore = embedTopicStore;
        this.remoteTopicStore = remoteTopicStore;
    }

    @Override
    public CompletableFuture<Topic> getTopic(String topicName) {
        if (embedTopicStore.exists(topicName)) {
            return embedTopicStore.getTopic(topicName);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return null;
        }

        return remoteTopicStore.getTopic(topicName);
    }
}
