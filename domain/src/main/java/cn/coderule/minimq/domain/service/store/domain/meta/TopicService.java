package cn.coderule.minimq.domain.service.store.domain.meta;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.model.meta.TopicMap;
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
