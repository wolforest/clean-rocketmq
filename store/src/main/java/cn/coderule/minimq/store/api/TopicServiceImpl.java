package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicStore;

public class TopicServiceImpl implements TopicService {
    private final TopicStore topicStore;

    public TopicServiceImpl(TopicStore topicStore) {
        this.topicStore = topicStore;
    }

    @Override
    public Topic getTopic(String topicName) {
        return topicStore.getTopic(topicName);
    }

    @Override
    public void putTopic(Topic topic) {
        topicStore.putTopic(topic);
    }

    @Override
    public void deleteTopic(String topicName) {
        topicStore.deleteTopic(topicName);
    }
}
