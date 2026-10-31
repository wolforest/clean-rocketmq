package com.wolfmq.store.domain.mq;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.core.lock.queue.DequeueLock;
import com.wolfmq.domain.server.store.api.MQStore;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.store.domain.commitlog.log.CommitLogProxy;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.ConsumeOrderService;
import com.wolfmq.domain.domain.mq.MQService;
import com.wolfmq.domain.domain.consumer.consume.InflightCounter;
import com.wolfmq.store.api.MQStoreImpl;
import com.wolfmq.store.domain.mq.ack.AckBootstrap;
import com.wolfmq.store.domain.mq.ack.AckService;
import com.wolfmq.store.domain.mq.ack.InvisibleService;
import com.wolfmq.store.domain.mq.queue.DequeueService;
import com.wolfmq.store.domain.mq.queue.EnqueueService;
import com.wolfmq.store.domain.mq.queue.MessageService;
import com.wolfmq.store.domain.mq.queue.OffsetService;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MQBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private DequeueLock dequeueLock;
    private InflightCounter inflightCounter;

    private AckBootstrap ackBootstrap;
    private EnqueueService enqueueService;
    private DequeueService dequeueService;
    private MessageService messageService;
    private OffsetService offsetService;
    private MQService mqService;

    public MQBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        dequeueLock = new DequeueLock();
        inflightCounter = new InflightCounter();
        context.register(dequeueLock);
        context.register(inflightCounter);

        initPubService();
        initAckManager();

        initSubService();
        initMQManager();
    }

    @Override
    public void start() throws Exception {
        CommitLogSynchronizer commitLogSynchronizer = context.getBean(CommitLogSynchronizer.class);
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
        AckService ackService = context.getBean(AckService.class);
        InvisibleService invisibleService = context.getBean(InvisibleService.class);
        ConsumeQueueManager consumeQueueManager = context.getBean(ConsumeQueueManager.class);

        MQStore MQStore = new MQStoreImpl(mqService, ackService, invisibleService, consumeQueueManager);
        context.register(MQStore, MQStore.class, ContextNamespace.API);
    }

    private void initAckManager() throws Exception {
        ackBootstrap = new AckBootstrap(context);
        ackBootstrap.initialize();
    }

    private void initPubService() {
        CommitLogProxy commitLogProxy = context.getBean(CommitLogProxy.class);
        StoreConfig storeConfig  = context.getBean(StoreConfig.class);
        ConsumeQueueManager consumeQueue = context.getBean(ConsumeQueueManager.class);

        this.enqueueService = new EnqueueService(storeConfig, commitLogProxy, consumeQueue);
        this.messageService = new MessageService(commitLogProxy, consumeQueue);
        context.register(this.enqueueService, EnqueueService.class);
        context.register(this.messageService, MessageService.class);
    }

    private void initSubService() {
        StoreConfig storeConfig  = context.getBean(StoreConfig.class);

        CommitLog commitLog = context.getBean(CommitLog.class);
        ConsumeQueueManager consumeQueue = context.getBean(ConsumeQueueManager.class);
        ConsumeOffsetService consumeOffsetService = context.getBean(ConsumeOffsetService.class);
        ConsumeOrderService orderService = context.getBean(ConsumeOrderService.class);

        AckService ackService = context.getBean(AckService.class);
        this.offsetService = new OffsetService(
            storeConfig, commitLog, ackService, consumeQueue, consumeOffsetService, orderService
        );
        context.register(offsetService, OffsetService.class);

        this.dequeueService = new DequeueService(
            storeConfig, dequeueLock, messageService, ackService, offsetService, inflightCounter, orderService
        );
        context.register(dequeueService, DequeueService.class);

        this.mqService = new DefaultMQService(enqueueService, dequeueService, messageService);
        context.register(mqService, MQService.class);
    }
}
