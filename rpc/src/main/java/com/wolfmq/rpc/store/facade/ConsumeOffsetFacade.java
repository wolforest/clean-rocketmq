package com.wolfmq.rpc.store.facade;

import com.wolfmq.common.domain.offset.dto.GroupResult;
import com.wolfmq.common.domain.offset.utils.OffsetFilter;
import com.wolfmq.common.domain.offset.dto.OffsetRequest;
import com.wolfmq.common.domain.offset.dto.OffsetResult;
import com.wolfmq.common.domain.offset.dto.TopicResult;

public interface ConsumeOffsetFacade {
    OffsetResult getOffset(OffsetRequest request);
    OffsetResult getAndRemove(OffsetRequest request);

    void putOffset(OffsetRequest request);

    void deleteByTopic(OffsetFilter filter);
    void deleteByGroup(OffsetFilter filter);

    TopicResult findTopicByGroup(OffsetFilter filter);
    GroupResult findGroupByTopic(OffsetFilter filter);
}
