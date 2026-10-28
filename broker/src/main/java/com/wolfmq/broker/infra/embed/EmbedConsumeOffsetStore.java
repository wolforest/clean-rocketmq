package com.wolfmq.broker.infra.embed;

import com.wolfmq.domain.domain.meta.offset.GroupResult;
import com.wolfmq.domain.domain.meta.offset.OffsetFilter;
import com.wolfmq.domain.domain.meta.offset.OffsetRequest;
import com.wolfmq.domain.domain.meta.offset.OffsetResult;
import com.wolfmq.domain.domain.meta.offset.TopicResult;
import com.wolfmq.rpc.store.facade.ConsumeOffsetFacade;
import com.wolfmq.domain.server.store.api.meta.ConsumeOffsetStore;

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
