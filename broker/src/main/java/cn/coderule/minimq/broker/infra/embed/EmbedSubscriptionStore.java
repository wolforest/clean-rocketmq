package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.model.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.store.api.SubscriptionStore;

public class EmbedSubscriptionStore implements SubscriptionStore {
    @Override
    public boolean existsGroup(String groupName) {
        return false;
    }

    @Override
    public SubscriptionGroup getGroup(String groupName) {
        return null;
    }

    @Override
    public void saveGroup(SubscriptionGroup group) {

    }

    @Override
    public void deleteGroup(String groupName, boolean cleanOffset) {

    }
}
