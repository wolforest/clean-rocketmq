package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.domain.model.subscription.SubscriptionGroup;
import java.util.concurrent.CompletableFuture;

public class RemoteSubscriptionStore extends AbstractRemoteStore {
    public RemoteSubscriptionStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }

    public CompletableFuture<SubscriptionGroup> getGroup(String topicName, String groupName) {
        return null;
    }
}
