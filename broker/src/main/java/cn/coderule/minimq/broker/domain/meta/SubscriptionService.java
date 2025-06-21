package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.broker.infra.store.BrokerSubscriptionStore;
import cn.coderule.minimq.domain.domain.model.consumer.subscription.SubscriptionGroup;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubscriptionService {
    private final BrokerSubscriptionStore subscriptionStore;

    public SubscriptionService(BrokerSubscriptionStore subscriptionStore) {
        this.subscriptionStore = subscriptionStore;
    }

    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        return subscriptionStore.getGroupAsync(topicName, groupName);
    }

    public SubscriptionGroup getGroup(String topicName, String groupName) {
        try {
            return getGroupAsync(topicName, groupName).get();
        } catch (Throwable t) {
            log.error("get subscription group error", t);
        }

        return null;
    }

    public boolean isConsumeOrderly(String topicName, String groupName) {
        SubscriptionGroup group  = getGroup(topicName, groupName);
        if (group == null) {
            return false;
        }

        return group.isConsumeMessageOrderly();
    }
}
