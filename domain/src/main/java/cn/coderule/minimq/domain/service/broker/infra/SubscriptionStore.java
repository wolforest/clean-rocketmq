package cn.coderule.minimq.domain.service.broker.infra;

import cn.coderule.minimq.domain.domain.model.consumer.subscription.SubscriptionGroup;
import java.util.concurrent.CompletableFuture;

public interface SubscriptionStore {
    CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName);
}
