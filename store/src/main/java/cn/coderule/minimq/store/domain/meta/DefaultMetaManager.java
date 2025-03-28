package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.manager.MetaManager;
import cn.coderule.minimq.store.api.TopicStoreImpl;
import cn.coderule.minimq.store.server.StoreContext;
import cn.coderule.minimq.store.server.bootstrap.StorePath;
import cn.coderule.minimq.store.server.bootstrap.StoreRegister;

public class DefaultMetaManager implements MetaManager {
    private DefaultTopicService topicService;

    @Override
    public void initialize() {
        initTopic();
    }

    @Override
    public void start() {
        injectDependency();
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void initTopic() {
        ConsumeOffsetService offsetService = StoreContext.getBean(ConsumeOffsetService.class);
        topicService = new DefaultTopicService(StorePath.getTopicPath(), offsetService);
        topicService.load();

        TopicStore topicApi = new TopicStoreImpl(topicService);
        StoreContext.registerAPI(topicApi, TopicStore.class);
    }

    private void injectDependency() {
        ConsumeQueueGateway consumeQueueGateway = StoreContext.getBean(ConsumeQueueGateway.class);
        StoreRegister storeRegister = StoreContext.getBean(StoreRegister.class);

        topicService.inject(consumeQueueGateway, storeRegister);
    }

    private void initConsumerOffset() {
        ConsumeOffsetService consumeOffsetStore = new DefaultConsumeOffsetService(StorePath.getConsumerOffsetPath());
    }
}
