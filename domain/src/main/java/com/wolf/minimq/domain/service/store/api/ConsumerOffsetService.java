package com.wolf.minimq.domain.service.store.api;

public interface ConsumerOffsetService {
    Long getOffset(String group, String topic, int queueId);
    Long getAndRemove(String group, String topic, int queueId);
    void putOffset(String group, String topic, int queueId, long offset);
}
