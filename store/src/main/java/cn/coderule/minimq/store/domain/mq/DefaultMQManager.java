package cn.coderule.minimq.store.domain.mq;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.domain.cluster.store.api.MQStore;
import cn.coderule.minimq.domain.domain.cluster.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.cluster.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.domain.cluster.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.domain.cluster.store.domain.meta.ConsumeOrderService;
import cn.coderule.minimq.domain.domain.cluster.store.domain.mq.MQManager;
import cn.coderule.minimq.domain.domain.cluster.store.domain.mq.MQService;
import cn.coderule.minimq.domain.domain.consumer.consume.InflightCounter;
import cn.coderule.minimq.store.api.MQStoreImpl;
import cn.coderule.minimq.store.domain.mq.ack.AckManager;
import cn.coderule.minimq.store.domain.mq.ack.AckService;
import cn.coderule.minimq.store.domain.mq.queue.DequeueService;
import cn.coderule.minimq.store.domain.mq.queue.EnqueueService;
import cn.coderule.minimq.store.domain.mq.queue.MessageService;
import cn.coderule.minimq.store.domain.mq.queue.OffsetService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import cn.coderule.minimq.store.server.ha.server.processor.CommitLogSynchronizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultMQManager implements MQManager {
    private DequeueLock dequeueLock;
    private InflightCounter inflightCounter;

    private AckManager ackManager;
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
        ackManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        dequeueLock.shutdown();
        ackManager.shutdown();
    }

    private void initMQManager() {
        AckService ackService = StoreContext.getBean(AckService.class);
        ConsumeQueueGateway consumeQueueGateway = StoreContext.getBean(ConsumeQueueGateway.class);

        MQStore MQStore = new MQStoreImpl(mqService, ackService, consumeQueueGateway);
        StoreContext.registerAPI(MQStore, MQStore.class);
    }

    private void initAckManager() throws Exception {
        ackManager = new AckManager();
        ackManager.initialize();
    }

    private void initPubService() {
        StoreConfig storeConfig  = StoreContext.getBean(StoreConfig.class);

        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        ConsumeQueueGateway consumeQueue = StoreContext.getBean(ConsumeQueueGateway.class);

        this.enqueueService = new EnqueueService(commitLog, consumeQueue);
        this.messageService = new MessageService(storeConfig, commitLog, consumeQueue);
        StoreContext.register(this.enqueueService, EnqueueService.class);
        StoreContext.register(this.messageService, MessageService.class);
    }

    private void initSubService() {
        StoreConfig storeConfig  = StoreContext.getBean(StoreConfig.class);

        CommitLog commitLog = StoreContext.getBean(CommitLog.class);
        ConsumeQueueGateway consumeQueue = StoreContext.getBean(ConsumeQueueGateway.class);
        ConsumeOffsetService consumeOffsetService = StoreContext.getBean(ConsumeOffsetService.class);
        ConsumeOrderService orderService = StoreContext.getBean(ConsumeOrderService.class);

        AckService ackService = StoreContext.getBean(AckService.class);
        this.offsetService = new OffsetService(
            storeConfig, commitLog, ackService, consumeQueue, consumeOffsetService, orderService
        );
        StoreContext.register(offsetService, OffsetService.class);

        this.dequeueService = new DequeueService(
            storeConfig, dequeueLock, messageService, ackService, offsetService, inflightCounter
        );
        StoreContext.register(dequeueService, DequeueService.class);

        this.mqService = new DefaultMQService(enqueueService, dequeueService, messageService);
        StoreContext.register(mqService, MQService.class);
    }
}
