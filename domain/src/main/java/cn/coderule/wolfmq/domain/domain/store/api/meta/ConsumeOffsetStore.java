package cn.coderule.wolfmq.domain.domain.store.api.meta;

import cn.coderule.wolfmq.domain.domain.meta.offset.GroupResult;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetFilter;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.wolfmq.domain.domain.meta.offset.TopicResult;

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
