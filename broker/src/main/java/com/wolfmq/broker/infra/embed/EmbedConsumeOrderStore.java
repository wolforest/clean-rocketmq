package com.wolfmq.broker.infra.embed;

import com.wolfmq.domain.domain.meta.order.OrderRequest;
import com.wolfmq.domain.server.store.api.meta.ConsumeOrderStore;
import com.wolfmq.rpc.store.facade.ConsumeOrderFacade;

public class EmbedConsumeOrderStore extends AbstractEmbedStore implements ConsumeOrderFacade {
    private final ConsumeOrderStore consumeOrderStore;

    public EmbedConsumeOrderStore(ConsumeOrderStore consumeOrderStore, EmbedLoadBalance loadBalance) {
        super(loadBalance);
        this.consumeOrderStore = consumeOrderStore;
    }

    @Override
    public boolean isLocked(OrderRequest request) {
        return consumeOrderStore.isLocked(request);
    }

    @Override
    public void lock(OrderRequest request) {
        consumeOrderStore.lock(request);
    }

    @Override
    public void unlock(OrderRequest request) {
        consumeOrderStore.unlock(request);
    }

    @Override
    public long commit(OrderRequest request) {
        return consumeOrderStore.commit(request);
    }

    @Override
    public void updateInvisible(OrderRequest request) {
        consumeOrderStore.updateInvisible(request);
    }
}
