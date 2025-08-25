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

    /**
     * get offset
     *  1. return reset offset if checkResetOffset is true
     *      and reset offset exists
     *  2. get offset from consume offset
     *      init consume offset if not exists
     *  3. get buffered offset
     *      return max(buffered offset, offset)
     *
     * @param request dequeue request
     * @return offset
     */
    public long getOffset(DequeueRequest request) {
        long offset;
        if (request.isCheckResetOffset()) {
            offset = getResetOffset(request);
            if (offset > 0) {
                return offset;
            }
        }

        offset = getConsumeOffset(request);
        if (offset < 0) {
            offset = initOffset(request);
        }

        return mergeBufferOffset(request, offset);
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

    private long getConsumeOffset(DequeueRequest request) {
        return consumeOffsetService.getOffset(
            request.getGroup(),
            request.getTopic(),
            request.getQueueId()
        );
    }

    /**
     * init and return consume offset
     *  1. get min offset from consume queue
     *          if consumeStrategy is CONSUME_FROM_START
     *  2. get max offset from consume queue
     *  3. commit offset if commitInitOffset is true
     *  4. return offset
     *
     * @param request dequeue request
     * @return offset
     */
    private long initOffset(DequeueRequest request) {
        if (request.isConsumeFromStart()) {
            return consumeQueue.getMinOffset(request.getTopic(), request.getQueueId());
        }

        return 0;
    }

    private long getResetOffset(DequeueRequest request) {
        return -1;
    }

    private long mergeBufferOffset(DequeueRequest request, long offset) {
        return -1;
    }


}
