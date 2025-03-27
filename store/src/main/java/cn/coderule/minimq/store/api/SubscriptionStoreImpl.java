package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.model.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.store.api.SubscriptionStore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubscriptionStoreImpl implements SubscriptionStore {
    @Override
    public boolean existsGroup(String topicName) {
        return false;
    }

    @Override
    public void getGroup(String groupName) {

    }

    @Override
    public void saveGroup(SubscriptionGroup group) {

    }

    @Override
    public void deleteGroup(String groupName, boolean cleanOffset) {

    }
}
