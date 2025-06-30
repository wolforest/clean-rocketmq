package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionRequest;
import cn.coderule.minimq.domain.service.broker.infra.meta.SubscriptionFacade;
import cn.coderule.minimq.domain.service.store.api.meta.SubscriptionStore;
import java.util.concurrent.CompletableFuture;

public class EmbedSubscriptionStore extends AbstractEmbedStore implements SubscriptionFacade {
    private final SubscriptionStore subscriptionStore;

    public EmbedSubscriptionStore(SubscriptionStore subscriptionStore, EmbedLoadBalance loadBalance) {
        super(loadBalance);
        this.subscriptionStore = subscriptionStore;
    }

    public boolean existsGroup(String topicName, String groupName) {
        return subscriptionStore.existsGroup(groupName);
    }

    public SubscriptionGroup getGroup(String topicName, String groupName) {
        return subscriptionStore.getGroup(groupName);
    }

    @Override
    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        return CompletableFuture.completedFuture(
            subscriptionStore.getGroup(groupName)
        );
    }

    @Override
    public void putGroup(SubscriptionRequest request) {
        subscriptionStore.saveGroup(request);
    }

    @Override
    public void saveGroup(SubscriptionRequest request) {
        subscriptionStore.saveGroup(request);
    }

    @Override
    public void deleteGroup(SubscriptionRequest request) {
        subscriptionStore.deleteGroup(request);
    }

}
