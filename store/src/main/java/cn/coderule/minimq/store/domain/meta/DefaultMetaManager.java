package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetStore;
import cn.coderule.minimq.domain.service.store.manager.MetaManager;
import cn.coderule.minimq.store.api.TopicStoreImpl;
import cn.coderule.minimq.store.server.StoreContext;
import cn.coderule.minimq.store.server.StorePath;

public class DefaultMetaManager implements MetaManager {
    @Override
    public void initialize() {
        initTopic();
    }



    @Override
    public void start() {

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
        cn.coderule.minimq.domain.service.store.domain.meta.TopicStore topicStore = new DefaultTopicStore(StorePath.getTopicPath());
        topicStore.load();

        TopicStore topicService = new TopicStoreImpl(topicStore);
        StoreContext.registerAPI(topicService, TopicStore.class);
    }

    private void initConsumerOffset() {
        ConsumeOffsetStore consumeOffsetStore = new DefaultConsumeOffsetStore(StorePath.getConsumerOffsetPath());
    }
}
