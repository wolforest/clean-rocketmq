package com.wolfmq.common.domain.offset;

import com.wolfmq.common.domain.meta.service.MetaService;
import java.util.Set;

/**
 * @renamed from ConsumeOffsetService to ConsumerOffsetService
 */
public interface ConsumerOffsetService extends MetaService {
    Long getOffset(String group, String topic, int queueId);
    Long getAndRemove(String group, String topic, int queueId);
    void putOffset(String group, String topic, int queueId, long offset);

    boolean containsResetOffset(String group, String topic, int queueId);

    void deleteByTopic(String topicName);
    void deleteByGroup(String groupName);

    Set<String> findTopicByGroup(String group);
    Set<String> findGroupByTopic(String topic);
}
