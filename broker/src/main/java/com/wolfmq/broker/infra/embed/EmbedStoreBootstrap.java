package com.wolfmq.broker.infra.embed;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.server.bootstrap.BrokerContext;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.server.store.api.MQStore;
import com.wolfmq.domain.server.store.api.TimerStore;
import com.wolfmq.domain.server.store.api.meta.ConsumeOffsetStore;
import com.wolfmq.domain.server.store.api.meta.SubscriptionStore;
import com.wolfmq.domain.server.store.api.meta.TopicStore;
import com.wolfmq.store.Store;
import com.wolfmq.store.server.bootstrap.StoreContext;
import com.wolfmq.store.server.bootstrap.StoreArgument;

public class EmbedStoreBootstrap implements Lifecycle {
    private BrokerConfig brokerConfig;

    private Store store;
    private EmbedLoadBalance loadBalance;

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        if (!this.brokerConfig.isEnableEmbedStore()) {
            return;
        }

        initStore();
        initLoadBalance();
        initEmbedServices();
    }

    @Override
    public void start() throws Exception {
        if (!this.brokerConfig.isEnableEmbedStore()) {
            return;
        }

        store.start();
    }

    @Override
    public void shutdown() throws Exception {
        if (!this.brokerConfig.isEnableEmbedStore()) {
            return;
        }

        store.shutdown();
    }

    @Override
    public void cleanup() throws Exception {
        store.cleanup();
    }

    private void initStore() throws Exception {
        StoreArgument storeArgument = new StoreArgument();
        store = new Store(storeArgument);
        store.initialize();

        ApplicationContext storeApi = StoreContext.API;
        BrokerContext.registerContext(storeApi);
    }

    private void initLoadBalance() {
        TopicStore topicStore = BrokerContext.getBean(TopicStore.class);
        SubscriptionStore subscriptionStore = BrokerContext.getBean(SubscriptionStore.class);

        loadBalance = new EmbedLoadBalance(brokerConfig, topicStore, subscriptionStore);
        BrokerContext.register(loadBalance);
    }

    private void initEmbedServices() {
        MQStore mqStore = BrokerContext.getBean(MQStore.class);
        EmbedMQStore embedMQStore = new EmbedMQStore(mqStore, loadBalance);
        BrokerContext.register(embedMQStore);

        TopicStore topicStore = BrokerContext.getBean(TopicStore.class);
        EmbedTopicStore embedTopicStore = new EmbedTopicStore(topicStore, loadBalance);
        BrokerContext.register(embedTopicStore);

        SubscriptionStore subscriptionStore = BrokerContext.getBean(SubscriptionStore.class);
        EmbedSubscriptionStore embedSubscriptionStore = new EmbedSubscriptionStore(subscriptionStore, loadBalance);
        BrokerContext.register(embedSubscriptionStore);

        ConsumeOffsetStore consumeOffsetStore = BrokerContext.getBean(ConsumeOffsetStore.class);
        EmbedConsumeOffsetStore embedConsumeOffsetStore = new EmbedConsumeOffsetStore(consumeOffsetStore, loadBalance);
        BrokerContext.register(embedConsumeOffsetStore);

        TimerStore timerStore = BrokerContext.getBean(TimerStore.class);
        EmbedTimerStore embedTimerStore = new EmbedTimerStore(timerStore, loadBalance);
        BrokerContext.register(embedTimerStore);
    }

}
