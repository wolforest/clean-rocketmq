package cn.coderule.minimq.domain.service.store.api;

import cn.coderule.minimq.domain.model.Topic;

public interface TopicService {
    Topic getTopic(String topicName);
    void putTopic(Topic topic);
    void deleteTopic(String topicName);
}
