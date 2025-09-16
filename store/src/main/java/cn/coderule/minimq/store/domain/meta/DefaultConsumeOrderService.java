package cn.coderule.minimq.store.domain.meta;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.meta.order.ConsumeOrder;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.domain.domain.store.domain.meta.ConsumeOrderService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultConsumeOrderService implements ConsumeOrderService {
    private final String storePath;
    private final OrderLockCleaner orderLockCleaner;

    private ConsumeOrder consumeOrder;

    public DefaultConsumeOrderService(
        StoreConfig storeConfig,
        String storePath,
        OrderLockCleaner orderLockCleaner
    ) {
        this.storePath = storePath;

        this.orderLockCleaner = orderLockCleaner;
        this.consumeOrder = new ConsumeOrder();
    }

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
        if (!FileUtil.exists(storePath)) {
            init();
            return;
        }

        String data = FileUtil.fileToString(storePath);
        decode(data);
    }

    @Override
    public void store() {
        String data = JSONUtil.toJSONString(consumeOrder);
        FileUtil.stringToFile(data, storePath);
    }

    private void cleanExpiredLock() {
        orderLockCleaner.clean(consumeOrder);
    }

    private void init() {
        if (consumeOrder != null) {
            return;
        }

        this.consumeOrder = new ConsumeOrder();
    }

    private void decode(String data) {
        if (StringUtil.isBlank(data)) {
            init();
            return;
        }

        this.consumeOrder = JSONUtil.parse(data, ConsumeOrder.class);
    }
}
