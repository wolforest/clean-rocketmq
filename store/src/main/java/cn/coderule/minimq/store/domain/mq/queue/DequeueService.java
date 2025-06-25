package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.domain.model.consumer.DequeueResult;
import cn.coderule.minimq.domain.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
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

    public CompletableFuture<DequeueResult> dequeueAsync(String group, String topic, int queueId, int num) {
        DequeueResult result = dequeue(group, topic, queueId, num);
        return CompletableFuture.supplyAsync(() -> result);
    }

    public DequeueResult dequeue(String group, String topic, int queueId, int num) {
        if (!dequeueLock.tryLock(group, topic, queueId)) {
            return DequeueResult.lockFailed();
        }

        try {
            long offset = consumeOffsetService.getOffset(group, topic, queueId);
            DequeueResult result = messageService.get(topic, queueId, offset, num);
            updateOffset(group, topic, queueId, result);

            return result;
        } catch (Throwable t) {
            log.error("dequeue error", t);
        } finally {
            dequeueLock.unlock(group, topic, queueId);
        }

        return DequeueResult.notFound();
    }

    private void updateOffset(String group, String topic, int queueId, DequeueResult result) {
        long newOffset = result.getMessageList().stream()
            .map(MessageBO::getQueueOffset)
            .max(Long::compareTo)
            .orElse(0L);

        if (newOffset == 0L) {
            return;
        }

        consumeOffsetService.putOffset(group, topic, queueId, newOffset);
    }
}
