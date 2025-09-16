package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.core.exception.DequeueException;
import cn.coderule.minimq.domain.domain.store.domain.meta.ConsumeOrderService;
import cn.coderule.minimq.domain.domain.consumer.consume.InflightCounter;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.store.domain.mq.ack.AckService;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DequeueService {
    private final StoreConfig storeConfig;
    private final DequeueLock dequeueLock;

    private final AckService ackService;
    private final MessageService messageService;
    private final OffsetService offsetService;
    private final InflightCounter inflightCounter;
    private final ConsumeOrderService consumeOrderService;

    public DequeueService(
        StoreConfig storeConfig,
        DequeueLock dequeueLock,
        MessageService messageService,
        AckService ackService,
        OffsetService offsetService,
        InflightCounter inflightCounter,
        ConsumeOrderService consumeOrderService
    ) {
        this.storeConfig = storeConfig;
        this.dequeueLock = dequeueLock;
        this.inflightCounter = inflightCounter;

        this.ackService = ackService;
        this.offsetService = offsetService;
        this.messageService = messageService;
        this.consumeOrderService = consumeOrderService;
    }

    public CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request) {
        DequeueResult result = dequeue(request);
        return CompletableFuture.supplyAsync(() -> result);
    }

    public DequeueResult dequeue(DequeueRequest request) {
        if (shouldSkip(request)) {
            return DequeueResult.flowControl();
        }
        if (!dequeueLock.tryLock(request)) {
            return DequeueResult.lockFailed();
        }

        try {
            return dequeueWithLock(request);
        } catch (DequeueException e) {
            return e.toResult();
        } catch (Throwable t) {
            return DequeueResult.unknownError(t);
        } finally {
            dequeueLock.unlock(request);
        }
    }

    private DequeueResult dequeueWithLock(DequeueRequest request) {
        getOffset(request);
        DequeueResult result = getMessage(request);
        updateOffset(request, result);
        addCheckpoint(request, result);
        increaseCounter(request, result);

        return result;
    }

    private boolean shouldSkip(DequeueRequest request) {
        if (hasTooManyInflightMessages(request)) {
            return true;
        }

        if (!request.isFifo()) {
            return false;
        }

        return isOrderQueueLocked(request);
    }

    private boolean isOrderQueueLocked(DequeueRequest request) {
        OrderRequest orderRequest = OrderRequest.builder()
            .topicName(request.getTopic())
            .consumerGroup(request.getGroup())
            .queueId(request.getQueueId())
            .attemptId(request.getAttemptId())
            .invisibleTime(request.getInvisibleTime())
            .build();

        boolean hasLock = consumeOrderService.isLocked(orderRequest);
        if (hasLock) {
            return true;
        }

        inflightCounter.clear(
            request.getTopic(),
            request.getGroup(),
            request.getQueueId()
        );
        return false;
    }

    private boolean hasTooManyInflightMessages(DequeueRequest request) {
        MessageConfig messageConfig = storeConfig.getMessageConfig();
        if (!messageConfig.isEnablePopThreshold()) {
            return false;
        }

        long inflight = inflightCounter.get(
            request.getTopic(), request.getGroup(), request.getQueueId()
        );

        boolean status = inflight > messageConfig.getPopInflightThreshold();
        if (status) {
            log.warn("Stop pop because too much message inflight,"
                    + "topic={}; group={}, queueId={}",
                request.getTopic(), request.getGroup(), request.getQueueId()
            );
        }

        return status;
    }

    private void getOffset(DequeueRequest request) {
        long offset = offsetService.getOffset(request);
        request.setOffset(offset);
    }

    private DequeueResult getMessage(DequeueRequest request) {
        DequeueResult result = getMessageAndOffset(request);

        if (result.isOffsetIllegal()) {
            return regetMessage(request, result);
        }

        return result;
    }

    private DequeueResult regetMessage(DequeueRequest request, DequeueResult result) {
        if (!result.hasNextOffset()) {
            return result;
        }

        if (request.getOffset() == result.getNextOffset()) {
            return result;
        }

        offsetService.updateOffset(request, result);

        request.setOffset(result.getNextOffset());
        return getMessageAndOffset(request);
    }

    private DequeueResult getMessageAndOffset(DequeueRequest request) {
        DequeueResult result = messageService.get(request);
        offsetService.setNextOffset(request, result);

        return result;
    }

    private void updateOffset(DequeueRequest request, DequeueResult result) {
        if (request.getOffset() == result.getNextOffset()) {
            return;
        }

        offsetService.updateOffset(request, result);
    }

    private void increaseCounter(DequeueRequest request, DequeueResult result) {
        inflightCounter.increment(
            request.getTopic(),
            request.getGroup(),
            request.getQueueId(),
            result.countMessage()
        );
    }

    private void addCheckpoint(DequeueRequest request, DequeueResult result) {
        if (!shouldStoreCheckpoint(request, result)) {
            return;
        }

        PopCheckPoint checkPoint = PopConverter.toCheckPoint(request, result);
        checkPoint.setBrokerName(storeConfig.getGroup());

        ackService.addCheckPoint(
            checkPoint,
            request.getReviveQueueId(),
            -1,
            result.getNextOffset()
        );
    }

    private boolean shouldStoreCheckpoint(DequeueRequest request, DequeueResult result) {
        if (request.isFifo()) {
            return false;
        }

        if (!result.isEmpty()) {
            return true;
        }

        if (result.getNextOffset() < 0) {
            return false;
        }

        MessageStatus status = result.getStatus();
        return MessageStatus.NO_MATCHED_MESSAGE.equals(status)
            || MessageStatus.OFFSET_FOUND_NULL.equals(status)
            || MessageStatus.MESSAGE_WAS_REMOVING.equals(status)
            || MessageStatus.NO_MATCHED_LOGIC_QUEUE.equals(status)
            ;
    }
}
