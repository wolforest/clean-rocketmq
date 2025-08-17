package cn.coderule.minimq.domain.service.store.domain.meta;

import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;

public interface ConsumeOrderService extends MetaService {
    void update(OrderRequest request);
    long commit(OrderRequest request);

    boolean checkBlock(OrderRequest request);
    void clearBlock(OrderRequest request);

    void updateInvisible(OrderRequest request);
}
