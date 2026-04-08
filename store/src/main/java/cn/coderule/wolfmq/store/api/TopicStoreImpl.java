package cn.coderule.wolfmq.store.api;

import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.meta.topic.TopicRequest;
import cn.coderule.wolfmq.domain.domain.store.api.meta.TopicStore;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;

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
    public void saveTopic(Topic topic) {
        topicService.saveTopic(topic);
    }

    @Override
    public void deleteTopic(String topicName) {
        topicService.deleteTopic(topicName);
    }

    @Override
    public void saveTopic(TopicRequest request) {
        topicService.saveTopic(request.getTopic());
    }

    @Override
    public void deleteTopic(TopicRequest request) {
        topicService.deleteTopic(request.getTopicName());
    }

    @Override
    public String getAllTopicJson() {
        return "";
    }
}
