package com.wolf.minimq.domain.service.store.domain.meta;

import com.wolf.minimq.domain.model.Topic;

public interface TopicManager extends MetaManager {
    Topic getTopic(String topicName);
    void putTopic(Topic topic);
    void deleteTopic(String topicName);
}
