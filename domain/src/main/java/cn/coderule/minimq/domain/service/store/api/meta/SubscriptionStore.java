package cn.coderule.minimq.domain.service.store.api.meta;

import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionRequest;

public interface SubscriptionStore {
    boolean existsGroup(String groupName);
    SubscriptionGroup getGroup(String groupName);

    void saveGroup(SubscriptionRequest request);
    void deleteGroup(SubscriptionRequest request);
}
