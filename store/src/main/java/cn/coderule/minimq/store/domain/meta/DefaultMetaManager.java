package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.service.store.api.SubscriptionStore;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.manager.MetaManager;
import cn.coderule.minimq.store.api.SubscriptionStoreImpl;
import cn.coderule.minimq.store.api.TopicStoreImpl;
import cn.coderule.minimq.store.server.StoreContext;
import cn.coderule.minimq.store.server.bootstrap.StorePath;
import cn.coderule.minimq.store.infra.StoreRegister;

public class DefaultMetaManager implements MetaManager {
    private ConsumeOffsetService offsetService;
    private DefaultTopicService topicService;
    private SubscriptionService subscriptionService;

    @Override
    public void initialize() {
        initConsumerOffset();
        initTopic();
        initSubscription();
    }

    @Override
    public void start() {
        injectDependency();
    }

    @Override
    public void shutdown() {
        offsetService.store();
        topicService.store();
        subscriptionService.store();
    }

    private void injectDependency() {
        ConsumeQueueGateway consumeQueueGateway = StoreContext.getBean(ConsumeQueueGateway.class);
        StoreRegister storeRegister = StoreContext.getBean(StoreRegister.class);

        topicService.inject(consumeQueueGateway, storeRegister);
    }

    private void initConsumerOffset() {
        offsetService = new DefaultConsumeOffsetService(StorePath.getConsumerOffsetPath());
        StoreContext.register(offsetService, ConsumeOffsetService.class);
    }

    private void initTopic() {
        topicService = new DefaultTopicService(StorePath.getTopicPath(), offsetService);
        StoreContext.register(topicService, TopicService.class);

        topicService.load();

        TopicStore topicApi = new TopicStoreImpl(topicService);
        StoreContext.registerAPI(topicApi, TopicStore.class);
    }

    private void initSubscription() {
        String storePath = StorePath.getSubscriptionGroupPath();
        subscriptionService = new DefaultSubscriptionService(storePath, offsetService);
        StoreContext.register(subscriptionService, SubscriptionService.class);

        subscriptionService.load();

        //SubscriptionStore subscriptionApi = new SubscriptionStoreImpl(subscriptionService);
    }
}
