package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.meta.offset.GroupResult;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetFilter;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.minimq.domain.domain.meta.offset.TopicResult;
import cn.coderule.minimq.rpc.store.facade.ConsumeOffsetFacade;
import cn.coderule.minimq.domain.service.store.api.meta.ConsumeOffsetStore;

public class EmbedConsumeOffsetStore extends AbstractEmbedStore implements ConsumeOffsetFacade {
    private final ConsumeOffsetStore consumeOffsetStore;

    public EmbedConsumeOffsetStore(ConsumeOffsetStore consumeOffsetStore, EmbedLoadBalance loadBalance) {
        super(loadBalance);
        this.consumeOffsetStore = consumeOffsetStore;
    }

    @Override
    public OffsetResult getOffset(OffsetRequest request) {
        return consumeOffsetStore.getOffset(request);
    }

    @Override
    public OffsetResult getAndRemove(OffsetRequest request) {
        return consumeOffsetStore.getAndRemove(request);
    }

    @Override
    public void putOffset(OffsetRequest request) {
        consumeOffsetStore.putOffset(request);
    }

    @Override
    public void deleteByTopic(OffsetFilter filter) {
        consumeOffsetStore.deleteByTopic(filter);
    }

    @Override
    public void deleteByGroup(OffsetFilter filter) {
        consumeOffsetStore.deleteByGroup(filter);
    }

    @Override
    public TopicResult findTopicByGroup(OffsetFilter filter) {
        return consumeOffsetStore.findTopicByGroup(filter);
    }

    @Override
    public GroupResult findGroupByTopic(OffsetFilter filter) {
        return consumeOffsetStore.findGroupByTopic(filter);
    }
}
