package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.domain.meta.order.ConsumeOrder;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOrderService;

public class DefaultConsumeOrderService implements ConsumeOrderService {
    private ConsumeOrder consumeOrder;

    @Override
    public void update(OrderRequest request) {
        consumeOrder.update(request);
    }

    @Override
    public long commit(OrderRequest request) {
        return consumeOrder.commit(request);
    }

    @Override
    public boolean isLocked(OrderRequest request) {
        return consumeOrder.isLocked(request);
    }

    @Override
    public void clearLock(OrderRequest request) {
        consumeOrder.clearLock(
            request.getTopicName(),
            request.getConsumerGroup(),
            request.getQueueId()
        );
    }

    @Override
    public void updateInvisible(OrderRequest request) {
        consumeOrder.updateVisible(request);
    }

    @Override
    public void load() {

    }

    @Override
    public void store() {

    }
}
