package cn.coderule.minimq.domain.service.store.api.meta;

import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;

public interface TopicStore {
    boolean exists(String topicName);
    Topic getTopic(String topicName);
    void saveTopic(Topic topic);
    void deleteTopic(String topicName);
}
