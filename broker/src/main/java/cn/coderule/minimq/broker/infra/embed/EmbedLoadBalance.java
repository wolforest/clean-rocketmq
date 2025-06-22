package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.config.server.BrokerConfig;

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
        if (!brokerConfig.isEnableEmbedStore()) {
            return false;
        }

        return topicStore.exists(topicName);
    }

    public boolean containsSubscription(String groupName) {
        return false;
    }
}
