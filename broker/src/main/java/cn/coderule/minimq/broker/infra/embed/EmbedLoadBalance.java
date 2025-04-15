package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.config.BrokerConfig;

public class EmbedLoadBalance {
    private final BrokerConfig brokerConfig;
    private final EmbedTopicStore topicStore;
    private final EmbedSubscriptionStore subscriptionStore;

    public EmbedLoadBalance(BrokerConfig brokerConfig, EmbedTopicStore topicStore, EmbedSubscriptionStore subscriptionStore) {
        this.brokerConfig = brokerConfig;
        this.topicStore = topicStore;
        this.subscriptionStore = subscriptionStore;
    }

    public boolean containsTopic(String topicName) {
        return false;
    }

    public boolean containsSubscription(String topicName) {
        return false;
    }
}
