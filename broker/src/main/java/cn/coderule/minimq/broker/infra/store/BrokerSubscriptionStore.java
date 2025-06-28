package cn.coderule.minimq.broker.infra.store;

import cn.coderule.minimq.broker.infra.embed.EmbedSubscriptionStore;
import cn.coderule.minimq.broker.infra.remote.RemoteSubscriptionStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.broker.infra.meta.SubscriptionStore;
import java.util.concurrent.CompletableFuture;

public class BrokerSubscriptionStore implements SubscriptionStore {
    private final BrokerConfig brokerConfig;
    private final EmbedSubscriptionStore embedSubscriptionStore;
    private final RemoteSubscriptionStore remoteSubscriptionStore;

    public BrokerSubscriptionStore(BrokerConfig brokerConfig, EmbedSubscriptionStore embedSubscriptionStore,
        RemoteSubscriptionStore remoteSubscriptionStore) {
        this.brokerConfig = brokerConfig;
        this.embedSubscriptionStore = embedSubscriptionStore;
        this.remoteSubscriptionStore = remoteSubscriptionStore;
    }

    @Override
    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        if (embedSubscriptionStore.existsGroup(groupName)) {
            return embedSubscriptionStore.getGroupAsync(topicName, groupName);
        }

        if (!brokerConfig.isEnableRemoteStore()) {
            return null;
        }

        return remoteSubscriptionStore.getGroupAsync(topicName, groupName);
    }

}
