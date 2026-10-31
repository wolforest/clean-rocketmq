package com.wolfmq.store.domain.mq.ack;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.core.lock.queue.DequeueLock;
import com.wolfmq.domain.domain.consumer.ack.AckBuffer;
import com.wolfmq.domain.domain.consumer.consume.InflightCounter;
import com.wolfmq.domain.domain.meta.topic.KeyBuilder;
import com.wolfmq.store.domain.meta.DefaultConsumeOffsetService;
import com.wolfmq.store.domain.meta.DefaultConsumeOrderService;
import com.wolfmq.store.domain.mq.queue.EnqueueService;
import cn.coderule.common.convention.container.ApplicationContext;

/**
 * @renamed from AckManager to AckBootstrap
 */
public class AckBootstrap implements Lifecycle {
    private final ApplicationContext context;

    private StoreConfig storeConfig;

    private String reviveTopic;
    private AckBuffer ackBuffer;
    private AckMerger ackMerger;

    private AckOffset offsetService;
    private AckService ackService;

    public AckBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        initLibrary();

        initAckService();
        initInvisibleService();
    }

    @Override
    public void start() throws Exception {
        ackMerger.start();
    }

    @Override
    public void shutdown() throws Exception {
        ackMerger.shutdown();
    }

    private void initLibrary() {
        this.storeConfig = context.getBean(StoreConfig.class);
        MessageConfig messageConfig = storeConfig.getMessage();

        this.reviveTopic = KeyBuilder.buildClusterReviveTopic(storeConfig.getCluster());

        this.ackBuffer = new AckBuffer(messageConfig);
        this.ackMerger = new AckMerger(messageConfig, reviveTopic, ackBuffer);

        this.offsetService = initOffsetService();
    }

    private AckOffset initOffsetService() {
        return new AckOffset(
            context.getBean(DequeueLock.class),
            context.getBean(InflightCounter.class),
            context.getBean(DefaultConsumeOffsetService.class),
            context.getBean(DefaultConsumeOrderService.class)
        );
    }

    private void initAckService() {
        ackService =  new AckService(
            storeConfig,
            reviveTopic,
            ackBuffer,
            context.getBean(EnqueueService.class),
            offsetService
        );
        context.register(ackService);
    }

    private void initInvisibleService() {
        InvisibleService invisibleService =  new InvisibleService(
            storeConfig,
            reviveTopic,
            ackService,
            context.getBean(EnqueueService.class),
            offsetService
        );

        context.register(invisibleService);
    }
}
