package cn.coderule.minimq.domain.service.store.domain.meta;

import cn.coderule.minimq.domain.model.Topic;

public interface TopicStore extends MetaStore {
    boolean exists(String topicName);
    Topic getTopic(String topicName);
    void putTopic(Topic topic);
    void deleteTopic(String topicName);
}
