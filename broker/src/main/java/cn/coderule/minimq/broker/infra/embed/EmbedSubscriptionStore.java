package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionRequest;
import cn.coderule.minimq.domain.service.broker.infra.meta.SubscriptionFacade;
import java.util.concurrent.CompletableFuture;

public class EmbedSubscriptionStore implements SubscriptionFacade {

    public EmbedSubscriptionStore(EmbedLoadBalance loadBalance) {
    }

    public boolean existsGroup(String topicName, String groupName) {
        return false;
    }

    public SubscriptionGroup getGroup(String topicName, String groupName) {
        return null;
    }

    @Override
    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        return null;
    }

    @Override
    public void putGroup(SubscriptionRequest request) {

    }

    @Override
    public void saveGroup(SubscriptionRequest request) {

    }

    @Override
    public void deleteGroup(SubscriptionRequest request) {

    }

}
