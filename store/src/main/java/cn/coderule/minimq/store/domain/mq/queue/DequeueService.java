package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.domain.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DequeueService {
    private final StoreConfig storeConfig;
    private final MessageService messageService;
    private final ConsumeOffsetService consumeOffsetService;
    private final DequeueLock dequeueLock;

    public DequeueService(
        StoreConfig storeConfig,
        DequeueLock dequeueLock,
        MessageService messageService,
        ConsumeOffsetService consumeOffsetService
    ) {
        this.storeConfig = storeConfig;
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
            return messageService.get(topic, queueId, offset, num);
        } catch (Throwable t) {
            log.error("dequeue error", t);
        } finally {
            dequeueLock.unlock(group, topic, queueId);
        }

        return DequeueResult.notFound();
    }


}
