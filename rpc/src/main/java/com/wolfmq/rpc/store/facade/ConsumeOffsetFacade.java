package com.wolfmq.rpc.store.facade;

import com.wolfmq.common.domain.meta.offset.GroupResult;
import com.wolfmq.common.domain.meta.offset.OffsetFilter;
import com.wolfmq.common.domain.meta.offset.OffsetRequest;
import com.wolfmq.common.domain.meta.offset.OffsetResult;
import com.wolfmq.common.domain.meta.offset.TopicResult;

public interface ConsumeOffsetFacade {
    OffsetResult getOffset(OffsetRequest request);
    OffsetResult getAndRemove(OffsetRequest request);

    void putOffset(OffsetRequest request);

    void deleteByTopic(OffsetFilter filter);
    void deleteByGroup(OffsetFilter filter);

    TopicResult findTopicByGroup(OffsetFilter filter);
    GroupResult findGroupByTopic(OffsetFilter filter);
}
