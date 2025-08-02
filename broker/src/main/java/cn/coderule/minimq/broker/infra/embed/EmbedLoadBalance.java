package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.service.store.api.meta.SubscriptionStore;
import cn.coderule.minimq.domain.service.store.api.meta.TopicStore;

public class EmbedLoadBalance {
    private final BrokerConfig brokerConfig;
    private final TopicStore topicStore;
    private final SubscriptionStore subscriptionStore;

    public EmbedLoadBalance(BrokerConfig brokerConfig, TopicStore topicStore, SubscriptionStore subscriptionStore) {
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
        if (!brokerConfig.isEnableEmbedStore()) {
            return false;
        }

        return subscriptionStore.existsGroup(groupName);
    }

    public boolean isEmbed(String storeGroup) {
        return brokerConfig.getGroup().equals(storeGroup);
    }
}
