package cn.coderule.wolfmq.store.domain.mq;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.core.lock.queue.DequeueLock;
import cn.coderule.wolfmq.domain.domain.store.api.MQStore;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOrderService;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.MQService;
import cn.coderule.wolfmq.domain.domain.consumer.consume.InflightCounter;
import cn.coderule.wolfmq.store.api.MQStoreImpl;
import cn.coderule.wolfmq.store.domain.mq.ack.AckBootstrap;
import cn.coderule.wolfmq.store.domain.mq.ack.AckService;
import cn.coderule.wolfmq.store.domain.mq.ack.InvisibleService;
import cn.coderule.wolfmq.store.domain.mq.queue.DequeueService;
import cn.coderule.wolfmq.store.domain.mq.queue.EnqueueService;
import cn.coderule.wolfmq.store.domain.mq.queue.MessageService;
import cn.coderule.wolfmq.store.domain.mq.queue.OffsetService;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import cn.coderule.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MQBootstrap implements Lifecycle {
    private DequeueLock dequeueLock;
    private InflightCounter inflightCounter;

    private AckBootstrap ackBootstrap;
    private EnqueueService enqueueService;
    private DequeueService dequeueService;
    private MessageService messageService;
    private OffsetService offsetService;
    private MQService mqService;

    @Override
    public void initialize() throws Exception {
        dequeueLock = new DequeueLock();
        inflightCounter = new InflightCounter();
        StoreContext.register(dequeueLock);
        StoreContext.register(inflightCounter);

        initPubService();
        initAckManager();

        initSubService();
        initMQManager();
    }

    @Override
    public void start() throws Exception {
        CommitLogSynchronizer commitLogSynchronizer = StoreContext.getBean(CommitLogSynchronizer.class);
        this.enqueueService.inject(commitLogSynchronizer);

        dequeueLock.start();
        ackBootstrap.start();
    }

    @Override
    public void shutdown() throws Exception {
        dequeueLock.shutdown();
        ackBootstrap.shutdown();
    }

    private void initMQManager() {
        AckService ackService = StoreContext.getBean(AckService.class);
        InvisibleService invisibleService = StoreContext.getBean(InvisibleService.class);
        ConsumeQueueManager consumeQueueManager = StoreContext.getBean(ConsumeQueueManager.class);

        MQStore MQStore = new MQStoreImpl(mqService, ackService, invisibleService, consumeQueueManager);
        StoreContext.registerAPI(MQStore, MQStore.class);
    }

    private void initAckManager() throws Exception {
        ackBootstrap = new AckBootstrap();
        ackBootstrap.initialize();
    }

    private void initPubService() {
        CommitLogManager commitLogManager = StoreContext.getBean(CommitLogManager.class);
        StoreConfig storeConfig  = StoreContext.getBean(StoreConfig.class);
        ConsumeQueueManager consumeQueue = StoreContext.getBean(ConsumeQueueManager.class);

        this.enqueueService = new EnqueueService(storeConfig, commitLogManager, consumeQueue);
        this.messageService = new MessageService(commitLogManager, consumeQueue);
        StoreContext.register(this.enqueueService, EnqueueService.class);
        StoreContext.register(this.messageService, MessageService.class);
    }

    private void initSubService() {
        StoreConfig storeConfig  = StoreContext.getBean(StoreConfig.class);

        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        ConsumeQueueManager consumeQueue = StoreContext.getBean(ConsumeQueueManager.class);
        ConsumeOffsetService consumeOffsetService = StoreContext.getBean(ConsumeOffsetService.class);
        ConsumeOrderService orderService = StoreContext.getBean(ConsumeOrderService.class);

        AckService ackService = StoreContext.getBean(AckService.class);
        this.offsetService = new OffsetService(
            storeConfig, commitLog, ackService, consumeQueue, consumeOffsetService, orderService
        );
        StoreContext.register(offsetService, OffsetService.class);

        this.dequeueService = new DequeueService(
            storeConfig, dequeueLock, messageService, ackService, offsetService, inflightCounter, orderService
        );
        StoreContext.register(dequeueService, DequeueService.class);

        this.mqService = new DefaultMQService(enqueueService, dequeueService, messageService);
        StoreContext.register(mqService, MQService.class);
    }
}
