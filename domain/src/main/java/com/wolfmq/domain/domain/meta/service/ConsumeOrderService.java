package com.wolfmq.domain.domain.meta.service;

import com.wolfmq.domain.domain.meta.order.OrderRequest;

public interface ConsumeOrderService extends MetaService {
    boolean isLocked(OrderRequest request);
    void lock(OrderRequest request);
    void unlock(OrderRequest request);

    long commit(OrderRequest request);
    void updateInvisible(OrderRequest request);
}
