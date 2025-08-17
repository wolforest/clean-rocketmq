package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DequeueService {
    private final MessageService messageService;
    private final ConsumeOffsetService consumeOffsetService;
    private final DequeueLock dequeueLock;

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
        String group = request.getGroup();
        String topic = request.getTopic();
        int queueId = request.getQueueId();
        int num = request.getNum();

        if (!dequeueLock.tryLock(group, topic, queueId)) {
            return DequeueResult.lockFailed();
        }

        try {
            long offset = getOffset(request);
            DequeueResult result = messageService.get(topic, queueId, offset, num);
            updateOffset(request, result);
            addCheckpoint(request, result);

            return result;
        } catch (Throwable t) {
            log.error("dequeue error", t);
        } finally {
            dequeueLock.unlock(group, topic, queueId);
        }

        return DequeueResult.notFound();
    }

    private long getOffset(DequeueRequest request) {
        return consumeOffsetService.getOffset(
            request.getGroup(),
            request.getTopic(),
            request.getQueueId()
        );
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
    }
}
