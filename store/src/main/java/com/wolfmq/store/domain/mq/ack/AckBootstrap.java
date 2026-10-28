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
import com.wolfmq.store.server.bootstrap.StoreContext;

/**
 * @renamed from AckManager to AckBootstrap
 */
public class AckBootstrap implements Lifecycle {
    private StoreConfig storeConfig;

    private String reviveTopic;
    private AckBuffer ackBuffer;
    private AckMerger ackMerger;

    private AckOffset offsetService;
    private AckService ackService;

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
        this.storeConfig = StoreContext.getBean(StoreConfig.class);
        MessageConfig messageConfig = storeConfig.getMessageConfig();

        this.reviveTopic = KeyBuilder.buildClusterReviveTopic(storeConfig.getCluster());

        this.ackBuffer = new AckBuffer(messageConfig);
        this.ackMerger = new AckMerger(messageConfig, reviveTopic, ackBuffer);

        this.offsetService = initOffsetService();
    }

    private AckOffset initOffsetService() {
        return new AckOffset(
            StoreContext.getBean(DequeueLock.class),
            StoreContext.getBean(InflightCounter.class),
            StoreContext.getBean(DefaultConsumeOffsetService.class),
            StoreContext.getBean(DefaultConsumeOrderService.class)
        );
    }

    private void initAckService() {
        ackService =  new AckService(
            storeConfig,
            reviveTopic,
            ackBuffer,
            StoreContext.getBean(EnqueueService.class),
            offsetService
        );
        StoreContext.register(ackService);
    }

    private void initInvisibleService() {
        InvisibleService invisibleService =  new InvisibleService(
            storeConfig,
            reviveTopic,
            ackService,
            StoreContext.getBean(EnqueueService.class),
            offsetService
        );

        StoreContext.register(invisibleService);
    }
}
