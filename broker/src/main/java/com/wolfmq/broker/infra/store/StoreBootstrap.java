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
        this.brokerConfig = context.getBean(BrokerConfig.class);

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
        EmbedMQStore embedMQStore = context.getBean(EmbedMQStore.class);
        RemoteMQStore remoteMQStore = context.getBean(RemoteMQStore.class);
        MQStore mqStore = new MQStore(brokerConfig, embedMQStore, remoteMQStore);
        context.register(mqStore);
    }

    private void initTopicStore() {
        EmbedTopicStore embedTopicStore = context.getBean(EmbedTopicStore.class);
        RemoteTopicStore remoteTopicStore = context.getBean(RemoteTopicStore.class);
        TopicStore topicStore = new TopicStore(brokerConfig, embedTopicStore, remoteTopicStore);
        context.register(topicStore);
    }

    private void initConsumeOffsetStore() {
        EmbedConsumeOffsetStore embedStore = context.getBean(EmbedConsumeOffsetStore.class);
        RemoteConsumeOffsetStore remoteStore = context.getBean(RemoteConsumeOffsetStore.class);
        ConsumeOffsetStore consumeOffsetStore = new ConsumeOffsetStore(brokerConfig, embedStore, remoteStore);
        context.register(consumeOffsetStore);
    }

    private void initSubscriptionStore() {
        EmbedSubscriptionStore embedStore = context.getBean(EmbedSubscriptionStore.class);
        RemoteSubscriptionStore remoteStore = context.getBean(RemoteSubscriptionStore.class);
        SubscriptionStore subscriptionStore = new SubscriptionStore(brokerConfig, embedStore, remoteStore);
        context.register(subscriptionStore);
    }

    private void initTimerStore() {
        EmbedTimerStore embedStore = context.getBean(EmbedTimerStore.class);
        RemoteTimerStore remoteStore = context.getBean(RemoteTimerStore.class);
        TimerStore timerStore = new TimerStore(brokerConfig, embedStore, remoteStore);
        context.register(timerStore);
    }
}
