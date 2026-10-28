package com.wolfmq.store.domain.meta;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.server.store.api.meta.ConsumeOffsetStore;
import com.wolfmq.domain.server.store.api.meta.SubscriptionStore;
import com.wolfmq.domain.server.store.api.meta.TopicStore;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.ConsumeOrderService;
import com.wolfmq.domain.domain.meta.service.SubscriptionService;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.store.api.ConsumeOffsetStoreImpl;
import com.wolfmq.store.api.SubscriptionStoreImpl;
import com.wolfmq.store.api.TopicStoreImpl;
import com.wolfmq.store.server.bootstrap.StoreContext;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.store.server.bootstrap.StoreRegister;

public class MetaBootstrap implements Lifecycle {
    private DefaultTopicService topicService;
    private SubscriptionService subscriptionService;

    private ConsumeOffsetService offsetService;
    private ConsumeOrderService orderService;

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
        ConsumeQueueManager consumeQueueManager = StoreContext.getBean(ConsumeQueueManager.class);
        StoreRegister storeRegister = StoreContext.getBean(StoreRegister.class);

        topicService.inject(consumeQueueManager, storeRegister);
    }

    private void initConsumerOffset() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        offsetService = new DefaultConsumeOffsetService(storeConfig, StorePath.getConsumerOffsetPath());
        StoreContext.register(offsetService, ConsumeOffsetService.class);

        offsetService.load();

        ConsumeOffsetStore offsetStore = new ConsumeOffsetStoreImpl(offsetService);
        StoreContext.registerAPI(offsetStore, ConsumeOffsetStore.class);
        StoreContext.register(offsetStore, ConsumeOffsetStore.class);
    }

    private void initTopic() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        topicService = new DefaultTopicService(storeConfig, StorePath.getTopicPath(), offsetService);
        StoreContext.register(topicService, TopicService.class);

        topicService.load();

        TopicStore topicApi = new TopicStoreImpl(topicService);
        StoreContext.registerAPI(topicApi, TopicStore.class);
        StoreContext.register(topicApi, TopicStore.class);
    }

    private void initSubscription() {
        String storePath = StorePath.getSubscriptionGroupPath();
        subscriptionService = new DefaultSubscriptionService(storePath, offsetService);
        StoreContext.register(subscriptionService, SubscriptionService.class);

        subscriptionService.load();

        SubscriptionStore subscriptionApi = new SubscriptionStoreImpl(subscriptionService);
        StoreContext.registerAPI(subscriptionApi, SubscriptionStore.class);
        StoreContext.register(subscriptionApi, SubscriptionStore.class);
    }

    private void initConsumeOrder() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        String storePath = StorePath.getConsumerOrderInfoPath();

        OrderLockCleaner cleaner = new OrderLockCleaner(topicService, subscriptionService);
        orderService = new DefaultConsumeOrderService(storeConfig, storePath, cleaner);
        StoreContext.register(orderService, ConsumeOrderService.class);

        orderService.load();
    }
}
