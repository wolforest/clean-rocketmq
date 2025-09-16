package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.store.api.meta.ConsumeOffsetStore;
import cn.coderule.minimq.domain.domain.store.api.meta.SubscriptionStore;
import cn.coderule.minimq.domain.domain.store.api.meta.TopicStore;
import cn.coderule.minimq.domain.domain.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.domain.store.domain.meta.ConsumeOrderService;
import cn.coderule.minimq.domain.domain.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.domain.store.domain.meta.MetaManager;
import cn.coderule.minimq.store.api.ConsumeOffsetStoreImpl;
import cn.coderule.minimq.store.api.SubscriptionStoreImpl;
import cn.coderule.minimq.store.api.TopicStoreImpl;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.domain.config.store.StorePath;
import cn.coderule.minimq.store.infra.StoreRegister;

public class DefaultMetaManager implements MetaManager {
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
        ConsumeQueueGateway consumeQueueGateway = StoreContext.getBean(ConsumeQueueGateway.class);
        StoreRegister storeRegister = StoreContext.getBean(StoreRegister.class);

        topicService.inject(consumeQueueGateway, storeRegister);
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
