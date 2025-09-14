package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.domain.consumer.ack.AckBuffer;
import cn.coderule.minimq.domain.domain.consumer.consume.InflightCounter;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.store.domain.meta.DefaultConsumeOffsetService;
import cn.coderule.minimq.store.domain.meta.DefaultConsumeOrderService;
import cn.coderule.minimq.store.domain.mq.queue.EnqueueService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class AckManager implements Lifecycle {
    private StoreConfig storeConfig;

    private String reviveTopic;
    private AckBuffer ackBuffer;
    private AckMerger ackMerger;

    private OffsetService offsetService;
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

    private OffsetService initOffsetService() {
        return new OffsetService(
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
