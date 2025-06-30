package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedSubscriptionStore;
import cn.coderule.minimq.broker.infra.remote.RemoteSubscriptionStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionRequest;
import cn.coderule.minimq.domain.service.broker.infra.meta.SubscriptionFacade;
import java.util.concurrent.CompletableFuture;

public class SubscriptionStore implements SubscriptionFacade {
    private final BrokerConfig brokerConfig;
    private final EmbedSubscriptionStore embedSubscriptionStore;
    private final RemoteSubscriptionStore remoteSubscriptionStore;

    public SubscriptionStore(BrokerConfig brokerConfig, EmbedSubscriptionStore embedSubscriptionStore,
        RemoteSubscriptionStore remoteSubscriptionStore) {
        this.brokerConfig = brokerConfig;
        this.embedSubscriptionStore = embedSubscriptionStore;
        this.remoteSubscriptionStore = remoteSubscriptionStore;
    }

    @Override
    public boolean existsGroup(String topicName, String groupName) {
        if (embedSubscriptionStore.existsGroup(topicName, groupName)) {
            return true;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return false;
        }

        return remoteSubscriptionStore.existsGroup(topicName, groupName);
    }

    @Override
    public SubscriptionGroup getGroup(String topicName, String groupName) {
        if (embedSubscriptionStore.existsGroup(topicName, groupName)) {
            return embedSubscriptionStore.getGroup(topicName, groupName);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return null;
        }
        return remoteSubscriptionStore.getGroup(topicName, groupName);
    }

    @Override
    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        if (embedSubscriptionStore.existsGroup(topicName, groupName)) {
            return embedSubscriptionStore.getGroupAsync(topicName, groupName);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return null;
        }

        return remoteSubscriptionStore.getGroupAsync(topicName, groupName);
    }

    @Override
    public void putGroup(SubscriptionRequest request) {
        if (embedSubscriptionStore.existsGroup(request.getTopicName(), request.getGroupName())) {
            embedSubscriptionStore.putGroup(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteSubscriptionStore.putGroup(request);
    }

    @Override
    public void saveGroup(SubscriptionRequest request) {
        if (embedSubscriptionStore.existsGroup(request.getTopicName(), request.getGroupName())) {
            embedSubscriptionStore.saveGroup(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteSubscriptionStore.saveGroup(request);
    }

    @Override
    public void deleteGroup(SubscriptionRequest request) {
        if (embedSubscriptionStore.existsGroup(request.getTopicName(), request.getGroupName())) {
            embedSubscriptionStore.deleteGroup(request);
            return;
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return;
        }

        remoteSubscriptionStore.deleteGroup(request);
    }

}
