package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.domain.domain.cluster.store.api.meta.ConsumeOrderStore;
import cn.coderule.minimq.domain.domain.cluster.store.domain.meta.ConsumeOrderService;

public class ConsumeOrderStoreImpl implements ConsumeOrderStore {
    private final ConsumeOrderService consumeOrderService;

    public ConsumeOrderStoreImpl(ConsumeOrderService consumeOrderService) {
        this.consumeOrderService = consumeOrderService;
    }

    @Override
    public boolean isLocked(OrderRequest request) {
        return consumeOrderService.isLocked(request);
    }

    @Override
    public void lock(OrderRequest request) {
        consumeOrderService.lock(request);
    }

    @Override
    public void unlock(OrderRequest request) {
        consumeOrderService.unlock(request);
    }

    @Override
    public long commit(OrderRequest request) {
        return consumeOrderService.commit(request);
    }

    @Override
    public void updateInvisible(OrderRequest request) {
        consumeOrderService.updateInvisible(request);
    }
}
