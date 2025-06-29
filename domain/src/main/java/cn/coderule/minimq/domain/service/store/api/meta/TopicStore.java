package cn.coderule.minimq.domain.service.store.api.meta;

import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.meta.topic.TopicRequest;

public interface TopicStore {
    boolean exists(String topicName);
    Topic getTopic(String topicName);
    void saveTopic(Topic topic);
    void deleteTopic(String topicName);

    void saveTopic(TopicRequest request);
    void deleteTopic(TopicRequest request);
}
