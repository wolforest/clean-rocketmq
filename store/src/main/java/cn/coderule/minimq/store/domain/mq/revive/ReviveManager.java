package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.service.common.ServerEvent;
import cn.coderule.minimq.domain.service.common.ServerEventBus;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import java.util.ArrayList;
import java.util.List;

public class ReviveManager implements Lifecycle {
    private final List<ReviveThread> reviveThreadList = new ArrayList<>();

    @Override
    public void initialize() {
        ReviveContext context = initContext();
        RetryService retryService = new RetryService(context);
        int queueNum = context.getMessageConfig().getReviveQueueNum();

        for (int i = 0; i < queueNum; i++) {
            ReviveThread task = createReviveThread(context, i, retryService);
            reviveThreadList.add(task);
        }
    }

    @Override
    public void start() {
        for (ReviveThread reviveThread : reviveThreadList) {
            reviveThread.start();
        }
    }

    @Override
    public void shutdown() {
        for (ReviveThread reviveThread : reviveThreadList) {
            reviveThread.shutdown();
        }
    }

    private ReviveContext initContext() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        String reviveTopic = KeyBuilder.buildClusterReviveTopic(storeConfig.getCluster());

        return ReviveContext.builder()
            .reviveTopic(reviveTopic)
            .storeConfig(storeConfig)
            .messageConfig(storeConfig.getMessageConfig())

            .mqService(StoreContext.getBean(MQService.class))
            .consumeQueueGateway(StoreContext.getBean(ConsumeQueueGateway.class))

            .topicService(StoreContext.getBean(TopicService.class))
            .subscriptionService(StoreContext.getBean(SubscriptionService.class))
            .consumeOffsetService(StoreContext.getBean(ConsumeOffsetService.class))
            .build();
    }

    private ReviveThread createReviveThread(ReviveContext context, int queueId, RetryService retryService) {
        ReviveThread reviveThread = new ReviveThread(
            context,
            queueId,
            new Reviver(context, queueId, retryService),
            new ReviveConsumer(context, queueId)
        );

        boolean isMaster = context.getStoreConfig().isMaster();
        reviveThread.setSkipRevive(!isMaster);

        ServerEventBus eventBus = StoreContext.getBean(ServerEventBus.class);
        eventBus.on(ServerEvent.BECOME_MASTER, (arg) -> reviveThread.setSkipRevive(false));
        eventBus.on(ServerEvent.BECOME_SLAVE, (arg) -> reviveThread.setSkipRevive(true));

        return reviveThread;
    }


}
