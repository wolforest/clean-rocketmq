package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.service.store.api.TopicService;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetStore;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicStore;
import cn.coderule.minimq.domain.service.store.manager.MetaManager;
import cn.coderule.minimq.store.api.TopicServiceImpl;
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
        TopicStore topicStore = new DefaultTopicStore(StorePath.getTopicPath());
        topicStore.load();

        TopicService topicService = new TopicServiceImpl(topicStore);
        StoreContext.registerAPI(topicService, TopicService.class);
    }

    private void initConsumerOffset() {
        ConsumeOffsetStore consumeOffsetStore = new DefaultConsumeOffsetStore(StorePath.getConsumerOffsetPath());
    }
}
