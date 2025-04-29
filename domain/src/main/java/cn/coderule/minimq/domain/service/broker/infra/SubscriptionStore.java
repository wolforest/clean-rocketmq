package cn.coderule.minimq.domain.service.broker.infra;

import cn.coderule.minimq.domain.domain.model.subscription.SubscriptionGroup;
import java.util.concurrent.CompletableFuture;

public interface SubscriptionStore {
    CompletableFuture<SubscriptionGroup> getGroup(String topicName, String groupName);
}
