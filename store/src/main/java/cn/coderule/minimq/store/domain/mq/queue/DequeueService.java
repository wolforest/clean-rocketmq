package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.core.exception.DequeueException;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
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

    public DequeueService(
        StoreConfig storeConfig,
        DequeueLock dequeueLock,
        MessageService messageService,
        AckService ackService,
        OffsetService offsetService
    ) {
        this.storeConfig = storeConfig;
        this.dequeueLock = dequeueLock;

        this.ackService = ackService;
        this.offsetService = offsetService;
        this.messageService = messageService;
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
    public boolean shouldStoreCheckpoint(DequeueRequest request, DequeueResult result) {
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
        offsetService.getNextOffset(request, result);

        return result;
    }

    private void updateOffset(DequeueRequest request, DequeueResult result) {
        if (request.getOffset() == result.getNextOffset()) {
            return;
        }

        offsetService.updateOffset(request, result);
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
}
