package cn.coderule.minimq.domain.domain.meta.order;

import java.util.ArrayList;
import java.util.List;

public class OrderUtils {
    private static final String TOPIC_GROUP_SEPARATOR = "@";

    public static String buildKey(String topic, String group) {
        return topic + TOPIC_GROUP_SEPARATOR + group;
    }

    public static String[] decodeKey(String key) {
        return key.split(TOPIC_GROUP_SEPARATOR);
    }

    public static List<Long> buildOffsetList(List<Long> offsetList) {
        List<Long> result = new ArrayList<>();
        if (offsetList.size() == 1) {
            result.addAll(offsetList);
            return result;
        }
        Long first = offsetList.get(0);
        result.add(first);
        for (int i = 1; i < offsetList.size(); i++) {
            result.add(offsetList.get(i) - first);
        }
        return result;
    }
}
