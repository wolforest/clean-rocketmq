package cn.coderule.wolfmq.test.benchmark.utils;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TopicUtils {
    private final int number;
    private final List<String> topicList;

    public TopicUtils(int number) {
        this.number = number;
        topicList = new ArrayList<>(number);
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
