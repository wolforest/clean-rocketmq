package cn.coderule.minimq.domain.utils;

import cn.coderule.minimq.domain.domain.core.enums.message.CleanupPolicy;
import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.model.meta.topic.TopicAttributes;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class CleanupUtils {
    public static boolean isCompaction(Optional<Topic> Topic) {
        return Objects.equals(CleanupPolicy.COMPACTION, getDeletePolicy(Topic));
    }

    public static CleanupPolicy getDeletePolicy(Optional<Topic> Topic) {
        if (Topic.isEmpty()) {
            return CleanupPolicy.valueOf(TopicAttributes.CLEANUP_POLICY_ATTRIBUTE.getDefaultValue());
        }

        String attributeName = TopicAttributes.CLEANUP_POLICY_ATTRIBUTE.getName();

        Map<String, String> attributes = Topic.get().getAttributes();
        if (attributes == null || attributes.isEmpty()) {
            return CleanupPolicy.valueOf(TopicAttributes.CLEANUP_POLICY_ATTRIBUTE.getDefaultValue());
        }

        if (attributes.containsKey(attributeName)) {
            return CleanupPolicy.valueOf(attributes.get(attributeName));
        } else {
            return CleanupPolicy.valueOf(TopicAttributes.CLEANUP_POLICY_ATTRIBUTE.getDefaultValue());
        }
    }
}
