package com.wolfmq.store.domain.meta;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.server.store.api.meta.ConsumeOffsetStore;
import com.wolfmq.domain.server.store.api.meta.SubscriptionStore;
import com.wolfmq.domain.server.store.api.meta.TopicStore;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueProxy;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.ConsumeOrderService;
import com.wolfmq.domain.domain.meta.service.SubscriptionService;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.store.api.ConsumeOffsetStoreImpl;
import com.wolfmq.store.api.SubscriptionStoreImpl;
import com.wolfmq.store.api.TopicStoreImpl;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.StoreState;
import com.wolfmq.store.server.bootstrap.StoreRegister;

public class MetaBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private DefaultTopicService topicService;
    private SubscriptionService subscriptionService;

    private ConsumeOffsetService offsetService;
    private ConsumeOrderService orderService;

    public MetaBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        initConsumerOffset();
        initTopic();
        initSubscription();
        initConsumeOrder();
    }

    @Override
    public void start() throws Exception {
        injectDependency();
    }

    @Override
    public void shutdown() throws Exception {
        offsetService.store();
        topicService.store();
        subscriptionService.store();
        orderService.store();
    }

    private void injectDependency() {
        ConsumeQueueProxy consumeQueueProxy = context.getBean(ConsumeQueueProxy.class);
        StoreRegister storeRegister = context.getBean(StoreRegister.class);

        topicService.inject(consumeQueueProxy, storeRegister);
    }

    private void initConsumerOffset() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        offsetService = new DefaultConsumeOffsetService(storeConfig, StorePath.getConsumerOffsetPath());
        context.register(offsetService, ConsumeOffsetService.class);

        offsetService.load();

        ConsumeOffsetStore offsetStore = new ConsumeOffsetStoreImpl(offsetService);
        context.register(offsetStore, ConsumeOffsetStore.class, ContextNamespace.API);
        context.register(offsetStore, ConsumeOffsetStore.class);
    }

    private void initTopic() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        StoreState storeState = context.getBean(StoreState.class);
        topicService = new DefaultTopicService(storeState, storeConfig, StorePath.getTopicPath(), offsetService);
        context.register(topicService, TopicService.class);

        topicService.load();

        TopicStore topicApi = new TopicStoreImpl(topicService);
        context.register(topicApi, TopicStore.class, ContextNamespace.API);
        context.register(topicApi, TopicStore.class);
    }

    private void initSubscription() {
        String storePath = StorePath.getSubscriptionGroupPath();
        StoreState storeState = context.getBean(StoreState.class);
        subscriptionService = new DefaultSubscriptionService(storeState, storePath, offsetService);
        context.register(subscriptionService, SubscriptionService.class);

        subscriptionService.load();

        SubscriptionStore subscriptionApi = new SubscriptionStoreImpl(subscriptionService);
        context.register(subscriptionApi, SubscriptionStore.class, ContextNamespace.API);
        context.register(subscriptionApi, SubscriptionStore.class);
    }

    private void initConsumeOrder() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        String storePath = StorePath.getConsumerOrderInfoPath();

        OrderLockCleaner cleaner = new OrderLockCleaner(topicService, subscriptionService);
        orderService = new DefaultConsumeOrderService(storeConfig, storePath, cleaner);
        context.register(orderService, ConsumeOrderService.class);

        orderService.load();
    }
}
