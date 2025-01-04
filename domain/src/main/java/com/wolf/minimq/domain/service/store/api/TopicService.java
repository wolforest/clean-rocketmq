package com.wolf.minimq.domain.service.store.api;

import com.wolf.minimq.domain.model.Topic;
import com.wolf.minimq.domain.service.store.domain.meta.MetaStore;

public interface TopicService {
    Topic getTopic(String topicName);
    void putTopic(Topic topic);
    void deleteTopic(String topicName);
}
