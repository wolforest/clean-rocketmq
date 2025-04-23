package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.broker.infra.store.BrokerSubscriptionStore;
import cn.coderule.minimq.domain.domain.model.subscription.SubscriptionGroup;
import java.util.concurrent.CompletableFuture;

public class SubscriptionService {
    private final BrokerSubscriptionStore subscriptionStore;

    public SubscriptionService(BrokerSubscriptionStore subscriptionStore) {
        this.subscriptionStore = subscriptionStore;
    }

    public CompletableFuture<SubscriptionGroup> getGroup(String topicName, String groupName) {
        return subscriptionStore.getGroup(topicName, groupName);
    }
}
