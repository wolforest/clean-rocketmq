package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.store.server.bootstrap.StoreRegister;

public class TopicStoreImpl implements TopicStore {
    private final TopicService topicService;
    private final StoreRegister storeRegister;

    public TopicStoreImpl(TopicService topicService, StoreRegister storeRegister) {
        this.topicService = topicService;
        this.storeRegister = storeRegister;
    }

    @Override
    public boolean exists(String topicName) {
        return topicService.exists(topicName);
    }

    @Override
    public Topic getTopic(String topicName) {
        return topicService.getTopic(topicName);
    }

    @Override
    public void saveTopic(Topic topic) {
        topicService.putTopic(topic);
        storeRegister.registerTopic(topic);
    }

    @Override
    public void deleteTopic(String topicName) {
        topicService.deleteTopic(topicName);
    }
}
