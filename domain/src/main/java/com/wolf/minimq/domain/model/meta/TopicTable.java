package com.wolf.minimq.domain.model.meta;

import com.alibaba.fastjson2.JSON;
import com.wolf.minimq.domain.enums.TagType;
import com.wolf.minimq.domain.model.Topic;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

@Data
public class TopicTable implements Serializable {
    private ConcurrentHashMap<String, Topic> topicTable = new ConcurrentHashMap<>();

    public Topic getTopic(String topicName) {
        return topicTable.get(topicName);
    }

    public void putTopic(Topic topic) {
        topicTable.put(topic.getTopicName(), topic);
    }
}
