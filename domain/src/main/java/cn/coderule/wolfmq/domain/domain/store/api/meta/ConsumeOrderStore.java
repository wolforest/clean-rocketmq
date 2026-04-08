package cn.coderule.wolfmq.domain.domain.store.api.meta;

import cn.coderule.wolfmq.domain.domain.meta.order.OrderRequest;

public interface ConsumeOrderStore {
    boolean isLocked(OrderRequest request);
    void lock(OrderRequest request);
    void unlock(OrderRequest request);

    long commit(OrderRequest request);
    void updateInvisible(OrderRequest request);
}
