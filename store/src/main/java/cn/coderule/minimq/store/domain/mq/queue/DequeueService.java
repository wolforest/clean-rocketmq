package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.domain.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DequeueService {
    private final StoreConfig storeConfig;
    private final ConsumeQueueGateway consumeQueueGateway;
    private final ConsumeOffsetService consumeOffsetService;
    private final CommitLog commitLog;
    private final DequeueLock dequeueLock;

    public DequeueService(
        StoreConfig storeConfig,
        CommitLog commitLog,
        DequeueLock dequeueLock,
        ConsumeQueueGateway consumeQueueGateway,
        ConsumeOffsetService consumeOffsetService
    ) {
        this.storeConfig = storeConfig;
        this.commitLog = commitLog;
        this.dequeueLock = dequeueLock;

        this.consumeQueueGateway = consumeQueueGateway;
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

        long offset = consumeOffsetService.getOffset(group, topic, queueId);

        return null;
    }


}
