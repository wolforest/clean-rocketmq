package com.wolfmq.common.domain.meta.service;

import com.wolfmq.common.domain.meta.subscription.SubscriptionGroup;
import com.wolfmq.common.domain.meta.subscription.SubscriptionMap;

public interface SubscriptionService extends MetaService {
    boolean existsGroup(String groupName);
    SubscriptionGroup getGroup(String groupName);
    void putGroup(SubscriptionGroup group);
    void saveGroup(SubscriptionGroup group);
    default void deleteGroup(String groupName) {
        this.deleteGroup(groupName, false);
    }

    void deleteGroup(String groupName, boolean cleanOffset);
    SubscriptionMap getSubscriptionMap();
}
