package cn.coderule.wolfmq.domain.domain.store.api.meta;

import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionRequest;

public interface SubscriptionStore {
    boolean existsGroup(String groupName);
    SubscriptionGroup getGroup(String groupName);

    void saveGroup(SubscriptionRequest request);
    void deleteGroup(SubscriptionRequest request);
}
