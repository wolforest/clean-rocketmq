package cn.coderule.minimq.domain.domain.meta.order;

public class KeyUtils {
    private static final String TOPIC_GROUP_SEPARATOR = "@";

    public static String buildKey(String topic, String group) {
        return topic + TOPIC_GROUP_SEPARATOR + group;
    }

    public static String[] decodeKey(String key) {
        return key.split(TOPIC_GROUP_SEPARATOR);
    }
}
