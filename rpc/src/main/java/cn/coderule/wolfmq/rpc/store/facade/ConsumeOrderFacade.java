package cn.coderule.wolfmq.rpc.store.facade;

import cn.coderule.wolfmq.domain.domain.meta.order.OrderRequest;

public interface ConsumeOrderFacade {
    boolean isLocked(OrderRequest request);
    void lock(OrderRequest request);
    void unlock(OrderRequest request);

    long commit(OrderRequest request);
    void updateInvisible(OrderRequest request);
}
