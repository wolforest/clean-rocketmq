package cn.coderule.wolfmq.rpc.store.facade;

import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionRequest;
import java.util.concurrent.CompletableFuture;

public interface SubscriptionFacade {
    boolean existsGroup(String topicName, String groupName);

    SubscriptionGroup getGroup(String topicName, String groupName);
    CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName);

    void putGroup(SubscriptionRequest request);
    void saveGroup(SubscriptionRequest request);
    void deleteGroup(SubscriptionRequest request);
}
