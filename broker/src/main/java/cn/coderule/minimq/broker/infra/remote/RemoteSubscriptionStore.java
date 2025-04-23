package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.domain.domain.model.subscription.SubscriptionGroup;

public class RemoteSubscriptionStore extends AbstractRemoteStore {
    public RemoteSubscriptionStore(RemoteLoadBalance loadBalance) {
        super(loadBalance);
    }

    public SubscriptionGroup getGroup(String topicName, String groupName) {
        return null;
    }
}
