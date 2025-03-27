package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.model.subscription.SubscriptionGroup;

public interface SubscriptionStore {
    boolean existsGroup(String topicName);
    void getGroup(String groupName);
    void saveGroup(SubscriptionGroup group);
    void deleteGroup(String groupName);
}
