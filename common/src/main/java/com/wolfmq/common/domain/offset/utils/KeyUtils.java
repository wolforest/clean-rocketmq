package com.wolfmq.common.domain.offset.utils;

public class KeyUtils {
    private static final String KEY_SEPARATOR = "@";

    public static String buildQueueKey(String topic, int queueId) {
        return topic
            + KEY_SEPARATOR
            + queueId;
    }

    public static String buildQueueShardKey(String topic, int queueId, int shardId) {
        return buildQueueKey(topic, queueId)
            + KEY_SEPARATOR
            + shardId;
    }

    public static String pad2(int value) {
        return String.format("%02d", value);
    }
}
