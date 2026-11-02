package com.wolfmq.common.server.store.api.meta;

import com.wolfmq.common.domain.meta.order.OrderRequest;

public interface ConsumeOrderStore {
    boolean isLocked(OrderRequest request);
    void lock(OrderRequest request);
    void unlock(OrderRequest request);

    long commit(OrderRequest request);
    void updateInvisible(OrderRequest request);
}
