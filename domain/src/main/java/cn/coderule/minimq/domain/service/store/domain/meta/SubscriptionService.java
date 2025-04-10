package cn.coderule.minimq.domain.service.store.domain.meta;

import cn.coderule.minimq.domain.domain.model.meta.SubscriptionMap;
import cn.coderule.minimq.domain.domain.model.subscription.SubscriptionGroup;

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
