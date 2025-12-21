package cn.coderule.wolfmq.test.benchmark.utils;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.wolfmq.test.manager.TopicManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;

@Data
public class TopicUtils implements Serializable {
    private final int number;
    private final List<String> topicList;

    public TopicUtils(int number) {
        this.number = number;
        topicList = new ArrayList<>(number);
    }

    public void createTopicList() {
        for (int i = 0; i < number; i++) {
            String topic = TopicManager.createUniqueTopic();
            TopicManager.createTopic(topic);
            topicList.add(topic);
        }
    }

    public void deleteTopicList() {
        for (String topic : topicList) {
            TopicManager.deleteTopic(topic);
        }
    }

    public String getTopic(int index) {
        if (index < 0 || index >= number) {
            return null;
        }
        return topicList.get(index);
    }

    public String getRandomTopic() {
        if (number <= 0 || CollectionUtil.isEmpty(topicList)) {
            return null;
        }

        int index = ThreadLocalRandom.current().nextInt(number);
        return topicList.get(index);
    }
}
