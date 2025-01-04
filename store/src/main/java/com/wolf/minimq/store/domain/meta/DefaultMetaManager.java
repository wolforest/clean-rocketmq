package com.wolf.minimq.store.domain.meta;

import com.wolf.minimq.domain.service.store.api.TopicService;
import com.wolf.minimq.domain.service.store.domain.meta.TopicStore;
import com.wolf.minimq.domain.service.store.manager.MetaManager;
import com.wolf.minimq.store.api.TopicServiceImpl;
import com.wolf.minimq.store.server.StoreContext;
import com.wolf.minimq.store.server.StorePath;

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
}
