package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.model.meta.SubscriptionMap;
import cn.coderule.minimq.domain.model.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultSubscriptionService implements SubscriptionService {
    @Getter
    private SubscriptionMap subscriptionMap;

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
