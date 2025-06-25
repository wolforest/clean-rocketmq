package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.broker.infra.SubscriptionStore;
import java.util.concurrent.CompletableFuture;

public class RemoteSubscriptionStore extends AbstractRemoteStore implements SubscriptionStore {
    public RemoteSubscriptionStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }

    @Override
    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        return null;
    }
}
