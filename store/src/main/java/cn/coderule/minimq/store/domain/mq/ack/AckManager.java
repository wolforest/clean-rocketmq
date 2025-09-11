package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.consumer.ack.AckBuffer;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.store.domain.mq.queue.EnqueueService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class AckManager implements Lifecycle {
    private AckMerger ackMerger;

    @Override
    public void initialize() throws Exception {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        AckBuffer ackBuffer = new AckBuffer(storeConfig.getMessageConfig());

        String reviveTopic = KeyBuilder.buildClusterReviveTopic(storeConfig.getCluster());
        ackMerger = new AckMerger(storeConfig.getMessageConfig(), reviveTopic, ackBuffer);

        EnqueueService enqueueService = StoreContext.getBean(EnqueueService.class);
        AckService ackService = new AckService(storeConfig, reviveTopic, ackBuffer, enqueueService);
        StoreContext.register(ackService);
    }

    @Override
    public void start() throws Exception {
        ackMerger.start();
    }

    @Override
    public void shutdown() throws Exception {
        ackMerger.shutdown();
    }
}
