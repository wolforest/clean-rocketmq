package com.wolfmq.common.domain.meta.service;

import com.wolfmq.common.domain.meta.topic.Topic;
import com.wolfmq.common.domain.meta.topic.utils.TopicMap;
import java.util.Map;

public interface TopicService extends MetaService {
    boolean exists(String topicName);
    Topic getTopic(String topicName);
    void saveTopic(Topic topic);
    void putTopic(Topic topic);
    void deleteTopic(String topicName);
    TopicMap getTopicMap();
    void updateOrderConfig(Map<String, String> orderMap);
}
