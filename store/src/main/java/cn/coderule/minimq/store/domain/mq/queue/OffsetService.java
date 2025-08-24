package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOrderService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OffsetService {
    private final StoreConfig storeConfig;
    private final ConsumeOffsetService consumeOffsetService;
    private final ConsumeOrderService consumeOrderService;
    private final ConsumeQueueGateway consumeQueue;

    public OffsetService(
        StoreConfig storeConfig,
        ConsumeOffsetService consumeOffsetService,
        ConsumeOrderService consumeOrderService,
        ConsumeQueueGateway consumeQueue
    ) {
        this.storeConfig = storeConfig;
        this.consumeQueue = consumeQueue;
        this.consumeOffsetService = consumeOffsetService;
        this.consumeOrderService = consumeOrderService;
    }

    public long getOffset(String group, String topic, int queueId) {
        return consumeOffsetService.getOffset(group, topic, queueId);
    }

    public void updateOffset(DequeueRequest request, DequeueResult result) {
        long newOffset = result.getNextOffset();
        if (newOffset <= 0L) {
            return;
        }

        consumeOffsetService.putOffset(
            request.getGroup(),
            request.getTopic(),
            request.getQueueId(),
            newOffset
        );
    }

}
