package cn.coderule.wolfmq.domain.domain.meta.topic;

import org.junit.jupiter.api.Test;

import static cn.coderule.wolfmq.domain.core.constant.MQConstants.RETRY_GROUP_TOPIC_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

class KeyBuilderTest {

    @Test
    void buildPopRetryTopicV1() {
        String result = KeyBuilder.buildPopRetryTopicV1("orderTopic", "consumerGroup");
        assertEquals(RETRY_GROUP_TOPIC_PREFIX + "consumerGroup_orderTopic", result);
    }

    @Test
    void buildPopRetryTopicV2() {
        String result = KeyBuilder.buildPopRetryTopicV2("orderTopic", "consumerGroup");
        assertEquals(RETRY_GROUP_TOPIC_PREFIX + "consumerGroup+orderTopic", result);
    }

    @Test
    void buildPopRetryTopic_enabledV2() {
        String result = KeyBuilder.buildPopRetryTopic("orderTopic", "consumerGroup", true);
        assertEquals(KeyBuilder.buildPopRetryTopicV2("orderTopic", "consumerGroup"), result);
    }

    @Test
    void buildPopRetryTopic_disabledV2() {
        String result = KeyBuilder.buildPopRetryTopic("orderTopic", "consumerGroup", false);
        assertEquals(KeyBuilder.buildPopRetryTopicV1("orderTopic", "consumerGroup"), result);
    }

    @Test
    void parseNormalTopic_v2RetryTopic() {
        String retryTopic = KeyBuilder.buildPopRetryTopicV2("orderTopic", "consumerGroup");
        String result = KeyBuilder.parseNormalTopic(retryTopic);
        assertEquals("orderTopic", result);
    }

    @Test
    void parseNormalTopic_normalTopic() {
        String result = KeyBuilder.parseNormalTopic("orderTopic");
        assertEquals("orderTopic", result);
    }

    @Test
    void parseNormalTopic_withGroupId() {
        String retryTopic = KeyBuilder.buildPopRetryTopicV1("orderTopic", "consumerGroup");
        String result = KeyBuilder.parseNormalTopic(retryTopic, "consumerGroup");
        assertEquals("orderTopic", result);
    }

    @Test
    void parseNormalTopic_withGroupId_v2() {
        String retryTopic = KeyBuilder.buildPopRetryTopicV2("orderTopic", "consumerGroup");
        String result = KeyBuilder.parseNormalTopic(retryTopic, "consumerGroup");
        assertEquals("orderTopic", result);
    }

    @Test
    void parseNormalTopic_normalTopicWithGroupId() {
        String result = KeyBuilder.parseNormalTopic("orderTopic", "consumerGroup");
        assertEquals("orderTopic", result);
    }

    @Test
    void parseGroup_v2RetryTopic() {
        String retryTopic = KeyBuilder.buildPopRetryTopicV2("orderTopic", "consumerGroup");
        String result = KeyBuilder.parseGroup(retryTopic);
        assertEquals("consumerGroup", result);
    }

    @Test
    void parseGroup_v1RetryTopic() {
        String retryTopic = KeyBuilder.buildPopRetryTopicV1("orderTopic", "consumerGroup");
        String result = KeyBuilder.parseGroup(retryTopic);
        assertEquals("consumerGroup_orderTopic", result);
    }

    @Test
    void isPopRetryTopicV2_true() {
        String retryTopic = KeyBuilder.buildPopRetryTopicV2("orderTopic", "consumerGroup");
        assertTrue(KeyBuilder.isPopRetryTopicV2(retryTopic));
    }

    @Test
    void isPopRetryTopicV2_false_v1() {
        String retryTopic = KeyBuilder.buildPopRetryTopicV1("orderTopic", "consumerGroup");
        assertFalse(KeyBuilder.isPopRetryTopicV2(retryTopic));
    }

    @Test
    void isPopRetryTopicV2_false_normal() {
        assertFalse(KeyBuilder.isPopRetryTopicV2("orderTopic"));
    }

    @Test
    void removeRetryPrefix_v1() {
        String retryTopic = KeyBuilder.buildPopRetryTopicV1("orderTopic", "consumerGroup");
        String result = KeyBuilder.removeRetryPrefix(retryTopic, "consumerGroup");
        assertEquals("orderTopic", result);
    }

    @Test
    void removeRetryPrefix_normal() {
        assertEquals("orderTopic", KeyBuilder.removeRetryPrefix("orderTopic", "consumerGroup"));
    }

    @Test
    void buildConsumeKey() {
        String result = KeyBuilder.buildConsumeKey("myTopic", "myGroup", 3);
        assertEquals("myTopic@myGroup@3", result);
    }

    @Test
    void buildPollingNotificationKey() {
        String result = KeyBuilder.buildPollingNotificationKey("myTopic", 5);
        assertEquals("myTopic@5", result);
    }

    @Test
    void buildClusterReviveTopic() {
        String result = KeyBuilder.buildClusterReviveTopic("myCluster");
        assertEquals("rmq_sys_REVIVE_LOG_myCluster", result);
    }
}