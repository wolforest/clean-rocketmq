package cn.coderule.minimq.domain.model.meta;

import cn.coderule.minimq.domain.model.Topic;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

@Data
public class TopicTable implements Serializable {
    private ConcurrentHashMap<String, Topic> topicMap = new ConcurrentHashMap<>();

    public Topic getTopic(String topicName) {
        return topicMap.get(topicName);
    }

    public void putTopic(Topic topic) {
        topicMap.put(topic.getTopicName(), topic);
    }

    public boolean exists(String topicName) {
        return topicMap.containsKey(topicName);
    }
}
