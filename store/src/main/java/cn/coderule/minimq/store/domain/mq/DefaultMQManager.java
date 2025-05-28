package cn.coderule.minimq.store.domain.mq;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.manager.MQManager;
import cn.coderule.minimq.domain.service.store.domain.MQService;
import cn.coderule.minimq.store.api.MQStoreImpl;
import cn.coderule.minimq.store.domain.mq.queue.DequeueService;
import cn.coderule.minimq.store.domain.mq.queue.EnqueueService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.commitlog.CommitLogSynchronizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMQManager implements MQManager {
    private DequeueLock dequeueLock;

    @Override
    public void initialize() {
        StoreConfig storeConfig  = StoreContext.getBean(StoreConfig.class);
        MessageConfig messageConfig = StoreContext.getBean(MessageConfig.class);
        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        ConsumeQueueGateway consumeQueueGateway = StoreContext.getBean(ConsumeQueueGateway.class);
        CommitLogSynchronizer commitLogSynchronizer = StoreContext.getBean(CommitLogSynchronizer.class);

        EnqueueService enqueueService = new EnqueueService(commitLog, consumeQueueGateway, commitLogSynchronizer);
        DequeueService dequeueService = new DequeueService(storeConfig, commitLog, dequeueLock, consumeQueueGateway);

        dequeueLock = new DequeueLock();

        MQService MQService = new DefaultMQService(enqueueService, dequeueService, messageConfig, commitLog, consumeQueueGateway);
        StoreContext.register(MQService, MQService.class);

        MQStore MQStore = new MQStoreImpl(messageConfig, MQService);
        StoreContext.registerAPI(MQStore, MQStore.class);
    }

    @Override
    public void start() {
        dequeueLock.start();
    }

    @Override
    public void shutdown() {
        dequeueLock.shutdown();
    }

}
