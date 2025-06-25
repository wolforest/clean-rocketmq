package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.minimq.domain.service.store.api.meta.ConsumeOffsetStore;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;

public class ConsumeOffsetStoreImpl implements ConsumeOffsetStore {
    private final ConsumeOffsetService offsetService;

    public ConsumeOffsetStoreImpl(ConsumeOffsetService offsetService) {
        this.offsetService = offsetService;
    }

    @Override
    public OffsetResult getOffset(OffsetRequest request) {
        long offset = offsetService.getOffset(
            request.getConsumerGroup(),
            request.getMessageQueue().getTopicName(),
            request.getMessageQueue().getQueueId()
        );
        return OffsetResult.build(offset);
    }

    @Override
    public OffsetResult getAndRemove(OffsetRequest request) {
        long offset = offsetService.getAndRemove(
            request.getConsumerGroup(),
            request.getMessageQueue().getTopicName(),
            request.getMessageQueue().getQueueId()
        );
        return OffsetResult.build(offset);
    }

    @Override
    public void putOffset(OffsetRequest request) {
        offsetService.putOffset(
            request.getConsumerGroup(),
            request.getMessageQueue().getTopicName(),
            request.getMessageQueue().getQueueId(),
            request.getNewOffset()
        );
    }
}
