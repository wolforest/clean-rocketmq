package com.wolfmq.domain.server.store.api.meta;

import com.wolfmq.domain.domain.meta.offset.GroupResult;
import com.wolfmq.domain.domain.meta.offset.OffsetFilter;
import com.wolfmq.domain.domain.meta.offset.OffsetRequest;
import com.wolfmq.domain.domain.meta.offset.OffsetResult;
import com.wolfmq.domain.domain.meta.offset.TopicResult;

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
