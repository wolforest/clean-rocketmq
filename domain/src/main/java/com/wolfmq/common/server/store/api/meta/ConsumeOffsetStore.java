package com.wolfmq.common.server.store.api.meta;

import com.wolfmq.common.domain.meta.offset.GroupResult;
import com.wolfmq.common.domain.meta.offset.OffsetFilter;
import com.wolfmq.common.domain.meta.offset.OffsetRequest;
import com.wolfmq.common.domain.meta.offset.OffsetResult;
import com.wolfmq.common.domain.meta.offset.TopicResult;

public interface ConsumeOffsetStore {
    OffsetResult getOffset(OffsetRequest request);
    OffsetResult getAndRemove(OffsetRequest request);

    void putOffset(OffsetRequest request);

    void deleteByTopic(OffsetFilter filter);
    void deleteByGroup(OffsetFilter filter);

    TopicResult findTopicByGroup(OffsetFilter filter);
    GroupResult findGroupByTopic(OffsetFilter filter);

    String getAllOffsetJson();
}
