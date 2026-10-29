package com.wolfmq.broker.infra.store;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.infra.embed.EmbedConsumeOffsetStore;
import com.wolfmq.broker.infra.embed.EmbedMQStore;
import com.wolfmq.broker.infra.embed.EmbedStoreBootstrap;
import com.wolfmq.broker.infra.embed.EmbedSubscriptionStore;
import com.wolfmq.broker.infra.embed.EmbedTimerStore;
import com.wolfmq.broker.infra.embed.EmbedTopicStore;
import com.wolfmq.broker.infra.remote.RemoteConsumeOffsetStore;
import com.wolfmq.broker.infra.remote.RemoteMQStore;
import com.wolfmq.broker.infra.remote.RemoteStoreBootstrap;
import com.wolfmq.broker.infra.remote.RemoteSubscriptionStore;
import com.wolfmq.broker.infra.remote.RemoteTimerStore;
import com.wolfmq.broker.infra.remote.RemoteTopicStore;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.domain.config.server.BrokerConfig;

public class StoreBootstrap implements Lifecycle {
    private final ApplicationContext context;
    private BrokerConfig brokerConfig;
    private final EmbedStoreBootstrap embedManager;
    private final RemoteStoreBootstrap remoteManager;

    public StoreBootstrap(ApplicationContext context) {
        this.context = context;
        this.embedManager = new EmbedStoreBootstrap(context);
        this.remoteManager = new RemoteStoreBootstrap(context);
    }

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);

        embedManager.initialize();
        remoteManager.initialize();

        initServices();
    }

    @Override
    public void start() throws Exception {
        embedManager.start();
        remoteManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        embedManager.shutdown();
        remoteManager.shutdown();
    }

    private void initServices() {
        initMQStore();
        initTimerStore();

        initTopicStore();
        initSubscriptionStore();
        initConsumeOffsetStore();
    }

    private void initMQStore() {
        EmbedMQStore embedMQStore = context.getBean(EmbedMQStore.class, ContextNamespace.APP);
        RemoteMQStore remoteMQStore = context.getBean(RemoteMQStore.class, ContextNamespace.APP);
        MQStore mqStore = new MQStore(brokerConfig, embedMQStore, remoteMQStore);
        context.register(mqStore, ContextNamespace.APP);
    }

    private void initTopicStore() {
        EmbedTopicStore embedTopicStore = context.getBean(EmbedTopicStore.class, ContextNamespace.APP);
        RemoteTopicStore remoteTopicStore = context.getBean(RemoteTopicStore.class, ContextNamespace.APP);
        TopicStore topicStore = new TopicStore(brokerConfig, embedTopicStore, remoteTopicStore);
        context.register(topicStore, ContextNamespace.APP);
    }

    private void initConsumeOffsetStore() {
        EmbedConsumeOffsetStore embedStore = context.getBean(EmbedConsumeOffsetStore.class, ContextNamespace.APP);
        RemoteConsumeOffsetStore remoteStore = context.getBean(RemoteConsumeOffsetStore.class, ContextNamespace.APP);
        ConsumeOffsetStore consumeOffsetStore = new ConsumeOffsetStore(brokerConfig, embedStore, remoteStore);
        context.register(consumeOffsetStore, ContextNamespace.APP);
    }

    private void initSubscriptionStore() {
        EmbedSubscriptionStore embedStore = context.getBean(EmbedSubscriptionStore.class, ContextNamespace.APP);
        RemoteSubscriptionStore remoteStore = context.getBean(RemoteSubscriptionStore.class, ContextNamespace.APP);
        SubscriptionStore subscriptionStore = new SubscriptionStore(brokerConfig, embedStore, remoteStore);
        context.register(subscriptionStore, ContextNamespace.APP);
    }

    private void initTimerStore() {
        EmbedTimerStore embedStore = context.getBean(EmbedTimerStore.class, ContextNamespace.APP);
        RemoteTimerStore remoteStore = context.getBean(RemoteTimerStore.class, ContextNamespace.APP);
        TimerStore timerStore = new TimerStore(brokerConfig, embedStore, remoteStore);
        context.register(timerStore, ContextNamespace.APP);
    }
}
