package com.wolfmq.broker.infra.embed;

import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.domain.server.store.api.MQStore;
import com.wolfmq.domain.server.store.api.TimerStore;
import com.wolfmq.domain.server.store.api.meta.ConsumeOffsetStore;
import com.wolfmq.domain.server.store.api.meta.SubscriptionStore;
import com.wolfmq.domain.server.store.api.meta.TopicStore;
import com.wolfmq.store.Store;
import com.wolfmq.store.server.bootstrap.StoreArgument;

public class EmbedStoreBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public EmbedStoreBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private BrokerConfig brokerConfig;

    private Store store;
    private EmbedLoadBalance loadBalance;

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);
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

        context.registerAll(
            store.getContext().getBeanMap(ContextNamespace.API),
            ContextNamespace.APP
        );
    }

    private void initLoadBalance() {
        TopicStore topicStore = context.getBean(TopicStore.class, ContextNamespace.APP);
        SubscriptionStore subscriptionStore = context.getBean(SubscriptionStore.class, ContextNamespace.APP);

        loadBalance = new EmbedLoadBalance(brokerConfig, topicStore, subscriptionStore);
        context.register(loadBalance, ContextNamespace.APP);
    }

    private void initEmbedServices() {
        MQStore mqStore = context.getBean(MQStore.class, ContextNamespace.APP);
        EmbedMQStore embedMQStore = new EmbedMQStore(mqStore, loadBalance);
        context.register(embedMQStore, ContextNamespace.APP);

        TopicStore topicStore = context.getBean(TopicStore.class, ContextNamespace.APP);
        EmbedTopicStore embedTopicStore = new EmbedTopicStore(topicStore, loadBalance);
        context.register(embedTopicStore, ContextNamespace.APP);

        SubscriptionStore subscriptionStore = context.getBean(SubscriptionStore.class, ContextNamespace.APP);
        EmbedSubscriptionStore embedSubscriptionStore = new EmbedSubscriptionStore(subscriptionStore, loadBalance);
        context.register(embedSubscriptionStore, ContextNamespace.APP);

        ConsumeOffsetStore consumeOffsetStore = context.getBean(ConsumeOffsetStore.class, ContextNamespace.APP);
        EmbedConsumeOffsetStore embedConsumeOffsetStore = new EmbedConsumeOffsetStore(consumeOffsetStore, loadBalance);
        context.register(embedConsumeOffsetStore, ContextNamespace.APP);

        TimerStore timerStore = context.getBean(TimerStore.class, ContextNamespace.APP);
        EmbedTimerStore embedTimerStore = new EmbedTimerStore(timerStore, loadBalance);
        context.register(embedTimerStore, ContextNamespace.APP);
    }

}
