package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;

public class TopicStoreImpl implements TopicStore {
    private final TopicService topicService;

    public TopicStoreImpl(TopicService topicService) {
        this.topicService = topicService;
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
    public void putTopic(Topic topic) {
        topicService.putTopic(topic);
    }

    @Override
    public void deleteTopic(String topicName) {
        topicService.deleteTopic(topicName);
    }
}
