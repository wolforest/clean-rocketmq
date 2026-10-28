package com.wolfmq.domain.server.store.api.meta;

import com.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import com.wolfmq.domain.domain.meta.subscription.SubscriptionRequest;

public interface SubscriptionStore {
    boolean existsGroup(String groupName);
    SubscriptionGroup getGroup(String groupName);

    void saveGroup(SubscriptionRequest request);
    void deleteGroup(SubscriptionRequest request);
}
