package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.domain.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;

public class DequeueService {
    private final StoreConfig storeConfig;
    private final ConsumeQueueGateway consumeQueueGateway;
    private final CommitLog commitLog;
    private final DequeueLock dequeueLock;

    public DequeueService(
        StoreConfig storeConfig,
        CommitLog commitLog,
        DequeueLock dequeueLock,
        ConsumeQueueGateway consumeQueueGateway
    ) {
        this.storeConfig = storeConfig;
        this.consumeQueueGateway = consumeQueueGateway;
        this.commitLog = commitLog;
        this.dequeueLock = dequeueLock;
    }

    public DequeueResult dequeue(String group, String topic, int queueId, int num) {
        if (!dequeueLock.tryLock(group, topic, queueId)) {
            return DequeueResult.lockFailed();
        }

        return null;
    }

}
