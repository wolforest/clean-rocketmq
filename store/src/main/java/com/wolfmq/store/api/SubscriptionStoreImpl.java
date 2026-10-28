package com.wolfmq.store.api;

import com.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import com.wolfmq.domain.domain.meta.subscription.SubscriptionRequest;
import com.wolfmq.domain.server.store.api.meta.SubscriptionStore;
import com.wolfmq.domain.domain.meta.service.SubscriptionService;
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
    public void saveGroup(SubscriptionRequest request) {
        subscriptionService.saveGroup(request.getGroup());
    }

    @Override
    public void deleteGroup(SubscriptionRequest request) {
        subscriptionService.deleteGroup(
            request.getGroupName(),
            request.isCleanOffset()
        );
    }
}
