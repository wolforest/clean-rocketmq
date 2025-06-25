package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.service.store.api.meta.ConsumeOffsetStore;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;

public class ConsumeOffsetStoreImpl implements ConsumeOffsetStore {
    private final ConsumeOffsetService offsetService;

    public ConsumeOffsetStoreImpl(ConsumeOffsetService offsetService) {
        this.offsetService = offsetService;
    }

    @Override
    public Long getOffset(String group, String topic, int queueId) {
        return offsetService.getOffset(group, topic, queueId);
    }

    @Override
    public Long getAndRemove(String group, String topic, int queueId) {
        return offsetService.getAndRemove(group, topic, queueId);
    }

    @Override
    public void putOffset(String group, String topic, int queueId, long offset) {
        offsetService.putOffset(group, topic, queueId, offset);
    }
}
