package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.domain.meta.order.ConsumeOrder;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOrderService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultConsumeOrderService implements ConsumeOrderService {

    private ConsumeOrder consumeOrder;
    private OrderLockCleaner orderLockCleaner;

    @Override
    public boolean isLocked(OrderRequest request) {
        return consumeOrder.isLocked(request);
    }

    @Override
    public void lock(OrderRequest request) {
        consumeOrder.lock(request);
    }

    @Override
    public void unlock(OrderRequest request) {
        consumeOrder.unlock(
            request.getTopicName(),
            request.getConsumerGroup(),
            request.getQueueId()
        );
    }

    @Override
    public long commit(OrderRequest request) {
        return consumeOrder.commit(request);
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

    private void cleanExpiredLock() {
        orderLockCleaner.clean(consumeOrder);
    }
}
