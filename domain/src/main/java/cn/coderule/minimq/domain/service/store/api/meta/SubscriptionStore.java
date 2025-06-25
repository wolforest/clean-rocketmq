package cn.coderule.minimq.domain.service.store.api.meta;

import cn.coderule.minimq.domain.domain.model.consumer.subscription.SubscriptionGroup;

public interface SubscriptionStore {
    boolean existsGroup(String groupName);
    SubscriptionGroup getGroup(String groupName);
    void saveGroup(SubscriptionGroup group);
    default void deleteGroup(String groupName) {
        this.deleteGroup(groupName, false);
    }

    void deleteGroup(String groupName, boolean cleanOffset);
}
