package cn.coderule.minimq.domain.service.store.domain.meta;

import cn.coderule.minimq.domain.model.meta.SubscriptionMap;
import cn.coderule.minimq.domain.model.subscription.SubscriptionGroup;

public interface SubscriptionService {
    boolean existsGroup(String topicName);
    void getGroup(String groupName);
    void saveGroup(SubscriptionGroup group);
    default void deleteGroup(String groupName) {
        this.deleteGroup(groupName, false);
    }

    void deleteGroup(String groupName, boolean cleanOffset);
    SubscriptionMap getSubscriptionMap();
}
