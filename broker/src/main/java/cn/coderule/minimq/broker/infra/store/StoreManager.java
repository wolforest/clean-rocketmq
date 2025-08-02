package cn.coderule.minimq.broker.infra.store;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.infra.embed.EmbedConsumeOffsetStore;
import cn.coderule.minimq.broker.infra.embed.EmbedMQStore;
import cn.coderule.minimq.broker.infra.embed.EmbedStoreManager;
import cn.coderule.minimq.broker.infra.embed.EmbedSubscriptionStore;
import cn.coderule.minimq.broker.infra.embed.EmbedTimerStore;
import cn.coderule.minimq.broker.infra.embed.EmbedTopicStore;
import cn.coderule.minimq.broker.infra.remote.RemoteConsumeOffsetStore;
import cn.coderule.minimq.broker.infra.remote.RemoteMQStore;
import cn.coderule.minimq.broker.infra.remote.RemoteStoreManager;
import cn.coderule.minimq.broker.infra.remote.RemoteSubscriptionStore;
import cn.coderule.minimq.broker.infra.remote.RemoteTimerStore;
import cn.coderule.minimq.broker.infra.remote.RemoteTopicStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class StoreManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private final EmbedStoreManager embedManager;
    private final RemoteStoreManager remoteManager;

    public StoreManager() {
        this.embedManager = new EmbedStoreManager();
        this.remoteManager = new RemoteStoreManager();
    }

    @Override
    public void initialize() throws Exception {
        this.brokerConfig = BrokerContext.getBean(BrokerConfig.class);

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
        EmbedMQStore embedMQStore = BrokerContext.getBean(EmbedMQStore.class);
        RemoteMQStore remoteMQStore = BrokerContext.getBean(RemoteMQStore.class);
        MQStore mqStore = new MQStore(brokerConfig, embedMQStore, remoteMQStore);
        BrokerContext.register(mqStore);
    }

    private void initTopicStore() {
        EmbedTopicStore embedTopicStore = BrokerContext.getBean(EmbedTopicStore.class);
        RemoteTopicStore remoteTopicStore = BrokerContext.getBean(RemoteTopicStore.class);
        TopicStore topicStore = new TopicStore(brokerConfig, embedTopicStore, remoteTopicStore);
        BrokerContext.register(topicStore);
    }

    private void initConsumeOffsetStore() {
        EmbedConsumeOffsetStore embedStore = BrokerContext.getBean(EmbedConsumeOffsetStore.class);
        RemoteConsumeOffsetStore remoteStore = BrokerContext.getBean(RemoteConsumeOffsetStore.class);
        ConsumeOffsetStore consumeOffsetStore = new ConsumeOffsetStore(brokerConfig, embedStore, remoteStore);
        BrokerContext.register(consumeOffsetStore);
    }

    private void initSubscriptionStore() {
        EmbedSubscriptionStore embedStore = BrokerContext.getBean(EmbedSubscriptionStore.class);
        RemoteSubscriptionStore remoteStore = BrokerContext.getBean(RemoteSubscriptionStore.class);
        SubscriptionStore subscriptionStore = new SubscriptionStore(brokerConfig, embedStore, remoteStore);
        BrokerContext.register(subscriptionStore);
    }

    private void initTimerStore() {
        EmbedTimerStore embedStore = BrokerContext.getBean(EmbedTimerStore.class);
        RemoteTimerStore remoteStore = BrokerContext.getBean(RemoteTimerStore.class);
        TimerStore timerStore = new TimerStore(brokerConfig, embedStore, remoteStore);
        BrokerContext.register(timerStore);
    }
}
