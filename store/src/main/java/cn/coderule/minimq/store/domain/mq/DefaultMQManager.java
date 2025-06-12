package cn.coderule.minimq.store.domain.mq;

import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.mq.MQManager;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import cn.coderule.minimq.store.api.MQStoreImpl;
import cn.coderule.minimq.store.domain.mq.queue.DequeueService;
import cn.coderule.minimq.store.domain.mq.queue.EnqueueService;
import cn.coderule.minimq.store.domain.mq.queue.MessageService;
import cn.coderule.minimq.store.domain.mq.revive.ReviveManager;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.commitlog.CommitLogSynchronizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMQManager implements MQManager {
    private DequeueLock dequeueLock;
    private ReviveManager reviveManager;

    @Override
    public void initialize() {
        dequeueLock = new DequeueLock();

        initMQManager();
        initReviveManager();

    }

    private void initMQManager() {
        MQService MQService = initMQService();
        MQStore MQStore = new MQStoreImpl(MQService);
        StoreContext.registerAPI(MQStore, MQStore.class);
    }

    private void initReviveManager() {
        reviveManager = new ReviveManager();
        reviveManager.initialize();
    }

    private MQService initMQService() {
        StoreConfig storeConfig  = StoreContext.getBean(StoreConfig.class);
        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        ConsumeQueueGateway consumeQueueGateway = StoreContext.getBean(ConsumeQueueGateway.class);
        CommitLogSynchronizer commitLogSynchronizer = StoreContext.getBean(CommitLogSynchronizer.class);
        ConsumeOffsetService consumeOffsetService = StoreContext.getBean(ConsumeOffsetService.class);

        EnqueueService enqueueService = new EnqueueService(commitLog, consumeQueueGateway, commitLogSynchronizer);
        MessageService messageService = new MessageService(storeConfig, commitLog, consumeQueueGateway);
        DequeueService dequeueService = new DequeueService(dequeueLock, messageService, consumeOffsetService);

        MQService MQService = new DefaultMQService(enqueueService, dequeueService, messageService);
        StoreContext.register(MQService, MQService.class);

        return MQService;
    }

    @Override
    public void start() {
        dequeueLock.start();
        reviveManager.start();
    }

    @Override
    public void shutdown() {
        dequeueLock.shutdown();
        reviveManager.shutdown();
    }

}
