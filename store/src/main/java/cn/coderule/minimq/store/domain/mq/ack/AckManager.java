package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.model.consumer.pop.ack.AckBuffer;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.service.store.api.AckStore;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import cn.coderule.minimq.store.api.AckStoreImpl;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;

public class AckManager implements Lifecycle {
    private AckMerger ackMerger;

    @Override
    public void initialize() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        AckBuffer ackBuffer = new AckBuffer(storeConfig.getMessageConfig());

        String reviveTopic = KeyBuilder.buildClusterReviveTopic(storeConfig.getCluster());
        ackMerger = new AckMerger(storeConfig.getMessageConfig(), reviveTopic, ackBuffer);

        MQService mqService = StoreContext.getBean(MQService.class);
        AckService ackService = new AckService(storeConfig, mqService, reviveTopic, ackBuffer);
        StoreContext.register(ackService);

        AckStore ackStore = new AckStoreImpl(ackService);
        StoreContext.registerAPI(ackStore, AckStore.class);
    }

    @Override
    public void start() {
        ackMerger.start();
    }

    @Override
    public void shutdown() {
        ackMerger.shutdown();
    }
}
