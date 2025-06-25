package cn.coderule.minimq.domain.service.store.api.meta;

public interface ConsumeOffsetStore {
    Long getOffset(String group, String topic, int queueId);
    Long getAndRemove(String group, String topic, int queueId);
    void putOffset(String group, String topic, int queueId, long offset);
}
