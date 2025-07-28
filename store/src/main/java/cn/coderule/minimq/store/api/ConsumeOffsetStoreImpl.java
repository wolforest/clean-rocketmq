package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.meta.offset.GroupResult;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetFilter;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.minimq.domain.domain.meta.offset.TopicResult;
import cn.coderule.minimq.domain.service.store.api.meta.ConsumeOffsetStore;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import java.util.Set;

public class ConsumeOffsetStoreImpl implements ConsumeOffsetStore {
    private final ConsumeOffsetService offsetService;

    public ConsumeOffsetStoreImpl(ConsumeOffsetService offsetService) {
        this.offsetService = offsetService;
    }

    @Override
    public OffsetResult getOffset(OffsetRequest request) {
        long offset = offsetService.getOffset(
            request.getConsumerGroup(),
            request.getTopicName(),
            request.getQueueId()
        );
        return OffsetResult.build(offset);
    }

    @Override
    public OffsetResult getAndRemove(OffsetRequest request) {
        long offset = offsetService.getAndRemove(
            request.getConsumerGroup(),
            request.getTopicName(),
            request.getQueueId()
        );
        return OffsetResult.build(offset);
    }

    @Override
    public void putOffset(OffsetRequest request) {
        offsetService.putOffset(
            request.getConsumerGroup(),
            request.getTopicName(),
            request.getQueueId(),
            request.getNewOffset()
        );
    }

    @Override
    public void deleteByTopic(OffsetFilter filter) {
        offsetService.deleteByTopic(filter.getTopic());
    }

    @Override
    public void deleteByGroup(OffsetFilter filter) {
        offsetService.deleteByGroup(filter.getGroup());
    }

    @Override
    public TopicResult findTopicByGroup(OffsetFilter filter) {
        Set<String> topicSet = offsetService.findTopicByGroup(filter.getGroup());
        return TopicResult.build(topicSet);
    }

    @Override
    public GroupResult findGroupByTopic(OffsetFilter filter) {
        Set<String> groupSet = offsetService.findGroupByTopic(filter.getTopic());
        return GroupResult.build(groupSet);
    }

    @Override
    public String getAllOffsetJson() {
        return "";
    }
}
