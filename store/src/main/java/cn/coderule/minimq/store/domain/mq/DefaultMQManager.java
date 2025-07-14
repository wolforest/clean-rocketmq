package cn.coderule.minimq.store.domain.mq;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import cn.coderule.minimq.domain.service.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.mq.MQManager;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import cn.coderule.minimq.store.api.MQStoreImpl;
import cn.coderule.minimq.store.domain.mq.ack.AckManager;
import cn.coderule.minimq.store.domain.mq.ack.AckService;
import cn.coderule.minimq.store.domain.mq.queue.DequeueService;
import cn.coderule.minimq.store.domain.mq.queue.EnqueueService;
import cn.coderule.minimq.store.domain.mq.queue.MessageService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.server.processor.CommitLogSynchronizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMQManager implements MQManager {
    private DequeueLock dequeueLock;
    private AckManager ackManager;

    @Override
    public void initialize() throws Exception {
        dequeueLock = new DequeueLock();

        initMQManager();
        initAckManager();
    }

    @Override
    public void start() throws Exception {
        dequeueLock.start();
        ackManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        dequeueLock.shutdown();
        ackManager.shutdown();
    }

    private void initMQManager() {
        MQService mqService = initMQService();
        AckService ackService = StoreContext.getBean(AckService.class);
        ConsumeQueueGateway consumeQueueGateway = StoreContext.getBean(ConsumeQueueGateway.class);

        MQStore MQStore = new MQStoreImpl(mqService, ackService, consumeQueueGateway);
        StoreContext.registerAPI(MQStore, MQStore.class);
    }


    private void initAckManager() {
        ackManager = new AckManager();
        try {
            ackManager.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

}
