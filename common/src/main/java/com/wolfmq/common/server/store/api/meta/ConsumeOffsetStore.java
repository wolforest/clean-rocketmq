package com.wolfmq.common.server.store.api.meta;

import com.wolfmq.common.domain.meta.offset.dto.GroupResult;
import com.wolfmq.common.domain.meta.offset.utils.OffsetFilter;
import com.wolfmq.common.domain.meta.offset.dto.OffsetRequest;
import com.wolfmq.common.domain.meta.offset.dto.OffsetResult;
import com.wolfmq.common.domain.meta.offset.dto.TopicResult;

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
