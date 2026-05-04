package cn.coderule.wolfmq.domain.domain.message.utils;

import cn.coderule.wolfmq.domain.core.enums.message.CleanupPolicy;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.meta.topic.TopicAttributes;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CleanupUtilsTest {

    @Test
    void isCompaction_withCompactionPolicy() {
        Topic topic = Topic.builder()
            .topicName("myTopic")
            .attributes(createAttributes("cleanup.policy", "COMPACTION"))
            .build();
        assertTrue(CleanupUtils.isCompaction(Optional.of(topic)));
    }

    @Test
    void isCompaction_withDeletePolicy() {
        Topic topic = Topic.builder()
            .topicName("myTopic")
            .attributes(createAttributes("cleanup.policy", "DELETE"))
            .build();
        assertFalse(CleanupUtils.isCompaction(Optional.of(topic)));
    }

    @Test
    void isCompaction_emptyTopic() {
        assertFalse(CleanupUtils.isCompaction(Optional.empty()));
    }

    @Test
    void getDeletePolicy_withCompactionPolicy() {
        Topic topic = Topic.builder()
            .topicName("myTopic")
            .attributes(createAttributes("cleanup.policy", "COMPACTION"))
            .build();
        assertEquals(CleanupPolicy.COMPACTION, CleanupUtils.getDeletePolicy(Optional.of(topic)));
    }

    @Test
    void getDeletePolicy_withDeletePolicy() {
        Topic topic = Topic.builder()
            .topicName("myTopic")
            .attributes(createAttributes("cleanup.policy", "DELETE"))
            .build();
        assertEquals(CleanupPolicy.DELETE, CleanupUtils.getDeletePolicy(Optional.of(topic)));
    }

    @Test
    void getDeletePolicy_emptyTopic_returnsDefault() {
        CleanupPolicy policy = CleanupUtils.getDeletePolicy(Optional.empty());
        assertEquals(CleanupPolicy.DELETE, policy);
    }

    @Test
    void getDeletePolicy_nullAttributes_returnsDefault() {
        Topic topic = Topic.builder()
            .topicName("myTopic")
            .attributes(null)
            .build();
        CleanupPolicy policy = CleanupUtils.getDeletePolicy(Optional.of(topic));
        assertEquals(CleanupPolicy.DELETE, policy);
    }

    @Test
    void getDeletePolicy_noCleanupPolicyAttribute_returnsDefault() {
        Topic topic = Topic.builder()
            .topicName("myTopic")
            .attributes(new HashMap<>())
            .build();
        CleanupPolicy policy = CleanupUtils.getDeletePolicy(Optional.of(topic));
        assertEquals(CleanupPolicy.DELETE, policy);
    }

    private Map<String, String> createAttributes(String key, String value) {
        Map<String, String> attrs = new HashMap<>();
        attrs.put(key, value);
        return attrs;
    }
}