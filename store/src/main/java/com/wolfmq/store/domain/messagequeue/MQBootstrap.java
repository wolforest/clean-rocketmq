package com.wolfmq.store.domain.messagequeue;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.core.lock.queue.DequeueLock;
import com.wolfmq.common.server.store.api.MQStore;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.store.domain.commitlog.CommitLogFacade;
import com.wolfmq.store.domain.consumequeue.service.ConsumeQueueFacade;
import com.wolfmq.common.domain.offset.consume.ConsumeOffsetService;
import com.wolfmq.common.domain.meta.service.ConsumeOrderService;
import com.wolfmq.common.domain.messagequeue.MQService;
import com.wolfmq.common.domain.consumer.consume.InflightCounter;
import com.wolfmq.store.api.MQStoreImpl;
import com.wolfmq.store.domain.messagequeue.ack.AckBootstrap;
import com.wolfmq.store.domain.messagequeue.ack.AckService;
import com.wolfmq.store.domain.messagequeue.ack.InvisibleService;
import com.wolfmq.store.domain.messagequeue.queue.DequeueService;
import com.wolfmq.store.domain.messagequeue.queue.EnqueueService;
import com.wolfmq.store.domain.messagequeue.queue.MessageService;
import com.wolfmq.store.domain.messagequeue.offset.OffsetService;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.common.core.constant.ContextNamespace;
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
        ConsumeQueueFacade consumeQueueFacade = context.getBean(ConsumeQueueFacade.class);

        MQStore MQStore = new MQStoreImpl(mqService, ackService, invisibleService, consumeQueueFacade);
        context.register(MQStore, MQStore.class, ContextNamespace.API);
    }

    private void initAckManager() throws Exception {
        ackBootstrap = new AckBootstrap(context);
        ackBootstrap.initialize();
    }

    private void initPubService() {
        CommitLogFacade commitLogFacade = context.getBean(CommitLogFacade.class);
        StoreConfig storeConfig  = context.getBean(StoreConfig.class);
        ConsumeQueueFacade consumeQueue = context.getBean(ConsumeQueueFacade.class);

        this.enqueueService = new EnqueueService(storeConfig, commitLogFacade, consumeQueue);
        this.messageService = new MessageService(commitLogFacade, consumeQueue);
        context.register(this.enqueueService, EnqueueService.class);
        context.register(this.messageService, MessageService.class);
    }

    private void initSubService() {
        StoreConfig storeConfig  = context.getBean(StoreConfig.class);

        CommitLog commitLog = context.getBean(CommitLog.class);
        ConsumeQueueFacade consumeQueue = context.getBean(ConsumeQueueFacade.class);
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
