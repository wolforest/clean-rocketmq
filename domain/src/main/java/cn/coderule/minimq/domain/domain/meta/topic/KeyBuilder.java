package cn.coderule.minimq.domain.domain.meta.topic;

import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.core.constant.PopConstants;

public class KeyBuilder {
    public static final int POP_ORDER_REVIVE_QUEUE = 999;
    private static final char POP_RETRY_SEPARATOR_V1 = '_';
    private static final char POP_RETRY_SEPARATOR_V2 = '+';
    private static final String POP_RETRY_REGEX_SEPARATOR_V2 = "\\+";

    /**
     * create retryTopicName by original topic, group
     *
     * @param topic original topic
     * @param cid original group
     * @return retryTopicName
     */
    public static String buildPopRetryTopic(String topic, String cid, boolean enableRetryV2) {
        if (enableRetryV2) {
            return buildPopRetryTopicV2(topic, cid);
        }
        return buildPopRetryTopicV1(topic, cid);
    }

    public static String buildPopRetryTopic(String topic, String cid) {
        return MQConstants.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V1 + topic;
    }

    public static String buildPopRetryTopicV2(String topic, String cid) {
        return MQConstants.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V2 + topic;
    }

    public static String buildPopRetryTopicV1(String topic, String group) {
        return MQConstants.RETRY_GROUP_TOPIC_PREFIX + group + POP_RETRY_SEPARATOR_V1 + topic;
    }

    public static String parseNormalTopic(String topic, String cid) {
        if (!topic.startsWith(MQConstants.RETRY_GROUP_TOPIC_PREFIX)) {
            return topic;
        }

        if (topic.startsWith(MQConstants.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V2)) {
            return topic.substring((MQConstants.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V2).length());
        }
        return topic.substring((MQConstants.RETRY_GROUP_TOPIC_PREFIX + cid + POP_RETRY_SEPARATOR_V1).length());
    }

    public static String parseNormalTopic(String retryTopic) {
        if (!isPopRetryTopicV2(retryTopic)) {
            return retryTopic;
        }

        String[] result = retryTopic.split(POP_RETRY_REGEX_SEPARATOR_V2);
        if (result.length == 2) {
            return result[1];
        }

        return retryTopic;
    }

    public static String parseGroup(String retryTopic) {
        if (!isPopRetryTopicV2(retryTopic)) {
            return retryTopic.substring(MQConstants.RETRY_GROUP_TOPIC_PREFIX.length());
        }

        String[] result = retryTopic.split(POP_RETRY_REGEX_SEPARATOR_V2);
        if (result.length == 2) {
            return result[0].substring(MQConstants.RETRY_GROUP_TOPIC_PREFIX.length());
        }

        return retryTopic.substring(MQConstants.RETRY_GROUP_TOPIC_PREFIX.length());
    }

    public static boolean isPopRetryTopicV2(String retryTopic) {
        return retryTopic.startsWith(MQConstants.RETRY_GROUP_TOPIC_PREFIX) && retryTopic.contains(String.valueOf(POP_RETRY_SEPARATOR_V2));
    }

    /**
     * remove retry topic prefix and group
     * @renamed from parseNormalTopic removeRetryPrefix
     *
     * @param topic topic
     * @param group group
     * @return topic name
     */
    public static String removeRetryPrefix(String topic, String group) {
        if (!topic.startsWith(MQConstants.RETRY_GROUP_TOPIC_PREFIX)) {
            return topic;
        }

        return topic.substring((MQConstants.RETRY_GROUP_TOPIC_PREFIX + group + "_").length());
    }

    /**
     * @renamed from buildPollingKey to buildConsumeKey
     */
    public static String buildConsumeKey(String topic, String group, int queueId) {
        return topic + PopConstants.SPLIT + group + PopConstants.SPLIT + queueId;
    }

    public static String buildPollingNotificationKey(String topic, int queueId) {
        return topic + PopConstants.SPLIT + queueId;
    }

    /**
     * Build cluster revive topic
     *
     * @param clusterName cluster name
     * @return revive topic
     */
    public static String buildClusterReviveTopic(String clusterName) {
        return PopConstants.REVIVE_TOPIC + clusterName;
    }
}
