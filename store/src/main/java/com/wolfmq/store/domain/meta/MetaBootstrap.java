package com.wolfmq.store.domain.meta;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.server.store.api.ConsumeOffsetStore;
import com.wolfmq.common.server.store.api.meta.SubscriptionStore;
import com.wolfmq.common.server.store.api.meta.TopicStore;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;
import com.wolfmq.common.domain.offset.service.ConsumeOffsetService;
import com.wolfmq.common.domain.meta.service.ConsumeOrderService;
import com.wolfmq.common.domain.meta.service.SubscriptionService;
import com.wolfmq.common.domain.meta.service.TopicService;
import com.wolfmq.store.api.ConsumeOffsetStoreImpl;
import com.wolfmq.store.api.SubscriptionStoreImpl;
import com.wolfmq.store.api.TopicStoreImpl;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.common.core.constant.ContextNamespace;
import com.wolfmq.common.server.store.StoreState;
import com.wolfmq.store.domain.offset.DefaultConsumeOffsetService;
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
        ConsumeQueueFacade consumeQueueFacade = context.getBean(ConsumeQueueFacade.class);
        StoreRegister storeRegister = context.getBean(StoreRegister.class);

        topicService.inject(consumeQueueFacade, storeRegister);
    }

    private void initConsumerOffset() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        offsetService = new DefaultConsumeOffsetService(storeConfig, storeConfig.getPath().getConsumerOffsetPath());
        context.register(offsetService, ConsumeOffsetService.class);

        offsetService.load();

        ConsumeOffsetStore offsetStore = new ConsumeOffsetStoreImpl(offsetService);
        context.register(offsetStore, ConsumeOffsetStore.class, ContextNamespace.API);
        context.register(offsetStore, ConsumeOffsetStore.class);
    }

    private void initTopic() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        StoreState storeState = context.getBean(StoreState.class);
        topicService = new DefaultTopicService(storeState, storeConfig, storeConfig.getPath().getTopicPath(), offsetService);
        context.register(topicService, TopicService.class);

        topicService.load();

        TopicStore topicApi = new TopicStoreImpl(topicService);
        context.register(topicApi, TopicStore.class, ContextNamespace.API);
        context.register(topicApi, TopicStore.class);
    }

    private void initSubscription() {
        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        String storePath = storeConfig.getPath().getSubscriptionGroupPath();
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
        String storePath = storeConfig.getPath().getConsumerOrderInfoPath();

        OrderLockCleaner cleaner = new OrderLockCleaner(topicService, subscriptionService);
        orderService = new DefaultConsumeOrderService(storeConfig, storePath, cleaner);
        context.register(orderService, ConsumeOrderService.class);

        orderService.load();
    }
}
