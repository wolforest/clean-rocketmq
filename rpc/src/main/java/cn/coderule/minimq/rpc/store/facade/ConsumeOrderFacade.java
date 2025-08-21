package cn.coderule.minimq.rpc.store.facade;

import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;

public interface ConsumeOrderFacade {
    boolean isLocked(OrderRequest request);
    void lock(OrderRequest request);
    void unlock(OrderRequest request);

    long commit(OrderRequest request);
    void updateInvisible(OrderRequest request);
}
