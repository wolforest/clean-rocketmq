package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.exception.DequeueException;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.store.domain.mq.ack.AckService;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DequeueService {
    private final DequeueLock dequeueLock;
    private final MessageService messageService;
    private final ConsumeOffsetService consumeOffsetService;

    private AckService ackService;
    private StoreConfig storeConfig;

    public DequeueService(
        DequeueLock dequeueLock,
        MessageService messageService,
        ConsumeOffsetService consumeOffsetService
    ) {
        this.dequeueLock = dequeueLock;

        this.messageService = messageService;
        this.consumeOffsetService = consumeOffsetService;
    }

    public CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request) {
        DequeueResult result = dequeue(request);
        return CompletableFuture.supplyAsync(() -> result);
    }

    public DequeueResult dequeue(DequeueRequest request) {
        if (!dequeueLock.tryLock(request)) {
            return DequeueResult.lockFailed();
        }

        try {
            getOffset(request);
            DequeueResult result = getMessage(request);
            updateOffset(request, result);
            addCheckpoint(request, result);

            return result;
        } catch (DequeueException e) {
            return e.toResult();
        } catch (Throwable t) {
            return DequeueResult.unknownError(t);
        } finally {
            dequeueLock.unlock(request);
        }
    }

    private void getOffset(DequeueRequest request) {
        long offset = consumeOffsetService.getOffset(
            request.getGroup(),
            request.getTopic(),
            request.getQueueId()
        );

        request.setOffset(offset);
    }

    private DequeueResult getMessage(DequeueRequest request) {
        DequeueResult result = messageService.get(request);

        if (result.isOffsetIllegal()) {
            // add checkpoint
            return regetMessage(request, result);
        }

        // empty result process

        return result;
    }

    private DequeueResult regetMessage(DequeueRequest request, DequeueResult result) {
        // todo retry
        return result;
    }

    private void updateOffset(DequeueRequest request, DequeueResult result) {
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

    private void addCheckpoint(DequeueRequest request, DequeueResult result) {
        if (request.isFifo()) {
            return;
        }

        request.setStoreGroup(storeConfig.getGroup());
        PopCheckPoint checkPoint = PopConverter.toCheckPoint(request, result);
        checkPoint.setBrokerName(storeConfig.getGroup());

        ackService.addCheckPoint(
            checkPoint,
            request.getReviveQueueId(),
            -1,
            result.getNextOffset()
        );
    }
}
