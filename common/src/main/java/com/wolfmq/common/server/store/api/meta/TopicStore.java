package com.wolfmq.common.server.store.api.meta;

import com.wolfmq.common.domain.meta.topic.Topic;
import com.wolfmq.common.domain.meta.topic.TopicRequest;

public interface TopicStore {
    boolean exists(String topicName);
    Topic getTopic(String topicName);
    void saveTopic(Topic topic);
    void deleteTopic(String topicName);

    void saveTopic(TopicRequest request);
    void deleteTopic(TopicRequest request);

    String getAllTopicJson();
}
