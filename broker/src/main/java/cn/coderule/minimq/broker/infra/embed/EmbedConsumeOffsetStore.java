package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.meta.offset.GroupResult;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetFilter;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.minimq.domain.domain.meta.offset.TopicResult;
import cn.coderule.minimq.domain.service.broker.infra.meta.ConsumeOffsetStore;

public class EmbedConsumeOffsetStore extends AbstractEmbedStore implements ConsumeOffsetStore {

    public EmbedConsumeOffsetStore(EmbedLoadBalance loadBalance) {
        super(loadBalance);
    }

    @Override
    public OffsetResult getOffset(OffsetRequest request) {
        return null;
    }

    @Override
    public OffsetResult getAndRemove(OffsetRequest request) {
        return null;
    }

    @Override
    public void putOffset(OffsetRequest request) {

    }

    @Override
    public void deleteByTopic(OffsetFilter filter) {

    }

    @Override
    public void deleteByGroup(OffsetFilter filter) {

    }

    @Override
    public TopicResult findTopicByGroup(OffsetFilter filter) {
        return null;
    }

    @Override
    public GroupResult findGroupByTopic(OffsetFilter filter) {
        return null;
    }
}
