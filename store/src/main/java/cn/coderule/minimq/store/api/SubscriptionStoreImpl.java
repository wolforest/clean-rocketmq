package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.model.consumer.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.store.api.meta.SubscriptionStore;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubscriptionStoreImpl implements SubscriptionStore {
    private final SubscriptionService subscriptionService;

    public SubscriptionStoreImpl(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    @Override
    public boolean existsGroup(String groupName) {
        return subscriptionService.existsGroup(groupName);
    }

    @Override
    public SubscriptionGroup getGroup(String groupName) {
        return subscriptionService.getGroup(groupName);
    }

    @Override
    public void saveGroup(SubscriptionGroup group) {
        subscriptionService.saveGroup(group);
    }

    @Override
    public void deleteGroup(String groupName, boolean cleanOffset) {
        subscriptionService.deleteGroup(groupName, cleanOffset);
    }
}
