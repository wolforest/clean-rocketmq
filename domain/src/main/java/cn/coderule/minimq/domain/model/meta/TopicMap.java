package cn.coderule.minimq.domain.model.meta;

import cn.coderule.minimq.domain.model.DataVersion;
import cn.coderule.minimq.domain.model.Topic;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

@Data
public class TopicMap implements Serializable {
    private ConcurrentHashMap<String, Topic> topicTable = new ConcurrentHashMap<>();
    private DataVersion dataVersion = new DataVersion();

    public boolean exists(String topicName) {
        return topicTable.containsKey(topicName);
    }

    public Topic getTopic(String topicName) {
        return topicTable.get(topicName);
    }

    public void saveTopic(Topic topic) {
        topicTable.put(topic.getTopicName(), topic);
    }

    public void deleteTopic(String topicName) {
        topicTable.remove(topicName);
    }


}
