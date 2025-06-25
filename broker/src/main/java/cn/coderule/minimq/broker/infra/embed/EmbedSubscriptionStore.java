package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.broker.infra.SubscriptionStore;
import java.util.concurrent.CompletableFuture;

public class EmbedSubscriptionStore implements SubscriptionStore {

    public EmbedSubscriptionStore(EmbedLoadBalance loadBalance) {
    }

    public boolean existsGroup(String groupName) {
        return false;
    }

    public SubscriptionGroup getGroup(String groupName) {
        return null;
    }

    @Override
    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        return null;
    }


}
