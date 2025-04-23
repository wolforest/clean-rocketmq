package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.model.subscription.SubscriptionGroup;

public class EmbedSubscriptionStore   {

    public EmbedSubscriptionStore(EmbedLoadBalance loadBalance) {
    }

    public boolean existsGroup(String groupName) {
        return false;
    }

    public SubscriptionGroup getGroup(String groupName) {
        return null;
    }

    public SubscriptionGroup getGroup(String topicName, String groupName) {
        return getGroup(groupName);
    }


}
