package com.wolfmq.store.api;

import com.wolfmq.common.domain.offset.dto.GroupResult;
import com.wolfmq.common.domain.offset.utils.OffsetFilter;
import com.wolfmq.common.domain.offset.dto.OffsetRequest;
import com.wolfmq.common.domain.offset.dto.OffsetResult;
import com.wolfmq.common.domain.offset.dto.TopicResult;
import com.wolfmq.common.server.store.api.ConsumeOffsetStore;
import com.wolfmq.common.domain.offset.consume.ConsumeOffsetService;
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
