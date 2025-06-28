package cn.coderule.minimq.domain.service.broker.infra.meta;

import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import java.util.concurrent.CompletableFuture;

public interface SubscriptionStore {
    CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName);
}
