package cn.coderule.wolfmq.domain.domain.message.utils;

import cn.coderule.wolfmq.domain.core.constant.MQConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NamespaceUtilsTest {

    @Test
    void withoutNamespace_normalTopic() {
        assertEquals("myTopic", NamespaceUtils.withoutNamespace("myTopic"));
    }

    @Test
    void withoutNamespace_namespaceTopic() {
        assertEquals("Topic_XXX", NamespaceUtils.withoutNamespace("MQ_INST_XX%Topic_XXX"));
    }

    @Test
    void withoutNamespace_retryTopic() {
        String retryTopic = MQConstants.RETRY_GROUP_TOPIC_PREFIX + "MQ_INST_XX%GID_XXX";
        String result = NamespaceUtils.withoutNamespace(retryTopic);
        assertEquals(MQConstants.RETRY_GROUP_TOPIC_PREFIX + "GID_XXX", result);
    }

    @Test
    void withoutNamespace_dlqTopic() {
        String dlqTopic = MQConstants.DLQ_GROUP_TOPIC_PREFIX + "MQ_INST_XX%GID_XXX";
        String result = NamespaceUtils.withoutNamespace(dlqTopic);
        assertEquals(MQConstants.DLQ_GROUP_TOPIC_PREFIX + "GID_XXX", result);
    }

    @Test
    void withoutNamespace_empty() {
        assertEquals("", NamespaceUtils.withoutNamespace(""));
    }

    @Test
    void withoutNamespace_withNamespaceParam_match() {
        String result = NamespaceUtils.withoutNamespace("MQ_INST_XX%Topic_XXX", "MQ_INST_XX");
        assertEquals("Topic_XXX", result);
    }

    @Test
    void withoutNamespace_withNamespaceParam_noMatch() {
        String result = NamespaceUtils.withoutNamespace("MQ_INST_XX%Topic_XXX", "MQ_INST_YY");
        assertEquals("MQ_INST_XX%Topic_XXX", result);
    }

    @Test
    void wrapNamespace_normal() {
        String result = NamespaceUtils.wrapNamespace("MQ_INST_XX", "Topic_XXX");
        assertEquals("MQ_INST_XX%Topic_XXX", result);
    }

    @Test
    void wrapNamespace_alreadyHasNamespace() {
        String result = NamespaceUtils.wrapNamespace("MQ_INST_XX", "MQ_INST_XX%Topic_XXX");
        assertEquals("MQ_INST_XX%Topic_XXX", result);
    }

    @Test
    void wrapNamespace_nullNamespace() {
        assertEquals("Topic_XXX", NamespaceUtils.wrapNamespace(null, "Topic_XXX"));
    }

    @Test
    void wrapNamespace_nullResource() {
        assertNull(NamespaceUtils.wrapNamespace("NS", null));
    }

    @Test
    void wrapNamespace_retryTopic() {
        String result = NamespaceUtils.wrapNamespace("MQ_INST_XX", MQConstants.RETRY_GROUP_TOPIC_PREFIX + "GID_XXX");
        assertTrue(result.startsWith(MQConstants.RETRY_GROUP_TOPIC_PREFIX));
        assertTrue(result.contains("MQ_INST_XX%GID_XXX"));
    }

    @Test
    void isAlreadyWithNamespace_true() {
        assertTrue(NamespaceUtils.isAlreadyWithNamespace("MQ_INST_XX%Topic_XXX", "MQ_INST_XX"));
    }

    @Test
    void isAlreadyWithNamespace_false() {
        assertFalse(NamespaceUtils.isAlreadyWithNamespace("Topic_XXX", "MQ_INST_XX"));
    }

    @Test
    void isAlreadyWithNamespace_nullNamespace() {
        assertFalse(NamespaceUtils.isAlreadyWithNamespace("Topic_XXX", null));
    }

    @Test
    void getNamespaceFromResource_withNamespace() {
        assertEquals("MQ_INST_XX", NamespaceUtils.getNamespaceFromResource("MQ_INST_XX%Topic_XXX"));
    }

    @Test
    void getNamespaceFromResource_noNamespace() {
        assertEquals("", NamespaceUtils.getNamespaceFromResource("Topic_XXX"));
    }

    @Test
    void getNamespaceFromResource_systemTopic() {
        assertEquals("", NamespaceUtils.getNamespaceFromResource("SCHEDULE_TOPIC_XXXX"));
    }

    @Test
    void isRetryTopic_true() {
        assertTrue(NamespaceUtils.isRetryTopic(MQConstants.RETRY_GROUP_TOPIC_PREFIX + "GID_XXX"));
    }

    @Test
    void isRetryTopic_false() {
        assertFalse(NamespaceUtils.isRetryTopic("Topic_XXX"));
    }

    @Test
    void isDLQTopic_true() {
        assertTrue(NamespaceUtils.isDLQTopic(MQConstants.DLQ_GROUP_TOPIC_PREFIX + "GID_XXX"));
    }

    @Test
    void isDLQTopic_false() {
        assertFalse(NamespaceUtils.isDLQTopic("Topic_XXX"));
    }

    @Test
    void withOutRetryAndDLQ_retryTopic() {
        String result = NamespaceUtils.withOutRetryAndDLQ(MQConstants.RETRY_GROUP_TOPIC_PREFIX + "GID_XXX");
        assertEquals("GID_XXX", result);
    }

    @Test
    void withOutRetryAndDLQ_dlqTopic() {
        String result = NamespaceUtils.withOutRetryAndDLQ(MQConstants.DLQ_GROUP_TOPIC_PREFIX + "GID_XXX");
        assertEquals("GID_XXX", result);
    }

    @Test
    void withOutRetryAndDLQ_normalTopic() {
        assertEquals("Topic_XXX", NamespaceUtils.withOutRetryAndDLQ("Topic_XXX"));
    }

    @Test
    void wrapNamespaceAndRetry() {
        String result = NamespaceUtils.wrapNamespaceAndRetry("MQ_INST_XX", "GID_XXX");
        assertTrue(result.startsWith(MQConstants.RETRY_GROUP_TOPIC_PREFIX));
    }

    @Test
    void wrapNamespaceAndRetry_nullGroup() {
        assertNull(NamespaceUtils.wrapNamespaceAndRetry("NS", null));
    }
}