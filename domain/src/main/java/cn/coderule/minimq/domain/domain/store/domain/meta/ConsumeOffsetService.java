package cn.coderule.minimq.domain.domain.store.domain.meta;

import java.util.Set;

public interface ConsumeOffsetService extends MetaService {
    Long getOffset(String group, String topic, int queueId);
    Long getAndRemove(String group, String topic, int queueId);
    void putOffset(String group, String topic, int queueId, long offset);

    boolean containsResetOffset(String group, String topic, int queueId);

    void deleteByTopic(String topicName);
    void deleteByGroup(String groupName);

    Set<String> findTopicByGroup(String group);
    Set<String> findGroupByTopic(String topic);
}
