package com.wolfmq.store.domain.mq;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.core.lock.queue.DequeueLock;
import com.wolfmq.domain.server.store.api.MQStore;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.store.domain.commitlog.log.CommitLogManager;
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
        context.register(dequeueLock, ContextNamespace.APP);
        context.register(inflightCounter, ContextNamespace.APP);

        initPubService();
        initAckManager();

        initSubService();
        initMQManager();
    }

    @Override
    public void start() throws Exception {
        CommitLogSynchronizer commitLogSynchronizer = context.getBean(CommitLogSynchronizer.class, ContextNamespace.APP);
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
        AckService ackService = context.getBean(AckService.class, ContextNamespace.APP);
        InvisibleService invisibleService = context.getBean(InvisibleService.class, ContextNamespace.APP);
        ConsumeQueueManager consumeQueueManager = context.getBean(ConsumeQueueManager.class, ContextNamespace.APP);

        MQStore MQStore = new MQStoreImpl(mqService, ackService, invisibleService, consumeQueueManager);
        context.register(MQStore, MQStore.class, ContextNamespace.API);
    }

    private void initAckManager() throws Exception {
        ackBootstrap = new AckBootstrap(context);
        ackBootstrap.initialize();
    }

    private void initPubService() {
        CommitLogManager commitLogManager = context.getBean(CommitLogManager.class, ContextNamespace.APP);
        StoreConfig storeConfig  = context.getBean(StoreConfig.class, ContextNamespace.APP);
        ConsumeQueueManager consumeQueue = context.getBean(ConsumeQueueManager.class, ContextNamespace.APP);

        this.enqueueService = new EnqueueService(storeConfig, commitLogManager, consumeQueue);
        this.messageService = new MessageService(commitLogManager, consumeQueue);
        context.register(this.enqueueService, EnqueueService.class, ContextNamespace.APP);
        context.register(this.messageService, MessageService.class, ContextNamespace.APP);
    }

    private void initSubService() {
        StoreConfig storeConfig  = context.getBean(StoreConfig.class, ContextNamespace.APP);

        CommitLog commitLog = context.getBean(CommitLog.class, ContextNamespace.APP);
        ConsumeQueueManager consumeQueue = context.getBean(ConsumeQueueManager.class, ContextNamespace.APP);
        ConsumeOffsetService consumeOffsetService = context.getBean(ConsumeOffsetService.class, ContextNamespace.APP);
        ConsumeOrderService orderService = context.getBean(ConsumeOrderService.class, ContextNamespace.APP);

        AckService ackService = context.getBean(AckService.class, ContextNamespace.APP);
        this.offsetService = new OffsetService(
            storeConfig, commitLog, ackService, consumeQueue, consumeOffsetService, orderService
        );
        context.register(offsetService, OffsetService.class, ContextNamespace.APP);

        this.dequeueService = new DequeueService(
            storeConfig, dequeueLock, messageService, ackService, offsetService, inflightCounter, orderService
        );
        context.register(dequeueService, DequeueService.class, ContextNamespace.APP);

        this.mqService = new DefaultMQService(enqueueService, dequeueService, messageService);
        context.register(mqService, MQService.class, ContextNamespace.APP);
    }
}
