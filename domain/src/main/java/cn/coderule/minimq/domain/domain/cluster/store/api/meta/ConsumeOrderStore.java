package cn.coderule.minimq.domain.domain.cluster.store.api.meta;

import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;

public interface ConsumeOrderStore {
    boolean isLocked(OrderRequest request);
    void lock(OrderRequest request);
    void unlock(OrderRequest request);

    long commit(OrderRequest request);
    void updateInvisible(OrderRequest request);
}
