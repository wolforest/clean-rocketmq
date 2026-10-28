package com.wolfmq.broker.infra.store;

import com.wolfmq.broker.infra.embed.EmbedTopicStore;
import com.wolfmq.broker.infra.remote.RemoteTopicStore;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.meta.topic.Topic;
import com.wolfmq.domain.domain.meta.topic.TopicRequest;
import com.wolfmq.rpc.store.facade.TopicFacade;
import java.util.concurrent.CompletableFuture;

public class TopicStore implements TopicFacade {
    private final BrokerConfig brokerConfig;
    private final EmbedTopicStore embedTopicStore;
    private final RemoteTopicStore remoteTopicStore;

    public TopicStore(BrokerConfig brokerConfig, EmbedTopicStore embedTopicStore, RemoteTopicStore remoteTopicStore) {
        this.brokerConfig = brokerConfig;
        this.embedTopicStore = embedTopicStore;
        this.remoteTopicStore = remoteTopicStore;
    }

    @Override
    public boolean exists(String topicName) {
        if (embedTopicStore.exists(topicName)) {
            return true;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return false;
        }
        return remoteTopicStore.exists(topicName);
    }

    @Override
    public CompletableFuture<Topic> getTopicAsync(String topicName) {
        if (embedTopicStore.exists(topicName)) {
            return embedTopicStore.getTopicAsync(topicName);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return null;
        }

        return remoteTopicStore.getTopicAsync(topicName);
    }

    @Override
    public Topic getTopic(String topicName) {
        if (embedTopicStore.exists(topicName)) {
            return embedTopicStore.getTopic(topicName);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return null;
        }

        return remoteTopicStore.getTopic(topicName);
    }

    @Override
    public void saveTopic(TopicRequest request) {
        if (!brokerConfig.isEnableEmbedStore()) {
            return;
        }

        embedTopicStore.saveTopic(request);
    }

    @Override
    public void deleteTopic(TopicRequest request) {
        if (!brokerConfig.isEnableEmbedStore()) {
            return;
        }

        embedTopicStore.deleteTopic(request);
    }
}
