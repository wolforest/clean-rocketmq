package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.service.common.ServerEvent;
import cn.coderule.minimq.domain.service.common.ServerEventBus;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import java.util.ArrayList;
import java.util.List;

public class ReviveManager implements Lifecycle {
    private final List<ReviveThread> reviveThreadList = new ArrayList<>();

    private ReviveContext initContext() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        String reviveTopic = KeyBuilder.buildClusterReviveTopic(storeConfig.getCluster());

        MQService mqService = StoreContext.getBean(MQService.class);
        ConsumeOffsetService consumeOffsetService = StoreContext.getBean(ConsumeOffsetService.class);
        TopicService topicService = StoreContext.getBean(TopicService.class);

        RetryService retryService = new RetryService(
            storeConfig,
            mqService,
            topicService,
            consumeOffsetService
        );

        return ReviveContext.builder()
            .storeConfig(storeConfig)
            .messageConfig(storeConfig.getMessageConfig())
            .reviveTopic(reviveTopic)

            .retryService(retryService)
            .mqService(StoreContext.getBean(MQService.class))
            .topicService(StoreContext.getBean(TopicService.class))
            .subscriptionService(StoreContext.getBean(SubscriptionService.class))
            .consumeOffsetService(StoreContext.getBean(ConsumeOffsetService.class))

            .build();
    }

    @Override
    public void initialize() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        MessageConfig messageConfig = StoreContext.getBean(MessageConfig.class);

        MQService mqService = StoreContext.getBean(MQService.class);
        ConsumeOffsetService consumeOffsetService = StoreContext.getBean(ConsumeOffsetService.class);
        TopicService topicService = StoreContext.getBean(TopicService.class);
        ServerEventBus eventBus = StoreContext.getBean(ServerEventBus.class);

        String reviveTopic = KeyBuilder.buildClusterReviveTopic(storeConfig.getCluster());
        RetryService retryService = new RetryService(
            storeConfig,
            mqService,
            topicService,
            consumeOffsetService
        );

        ReviveContext context = initContext();

        for (int i = 0; i < messageConfig.getReviveThreadNum(); i++) {
            ReviveConsumer consumer = new ReviveConsumer(context, i);
            Reviver reviver = new Reviver(context, i);

            ReviveThread reviveThread = new ReviveThread(
                storeConfig,
                reviveTopic,
                i,
                mqService,
                consumeOffsetService
            );


            eventBus.on(ServerEvent.BECOME_MASTER, (arg) -> reviveThread.setSkipRevive(false));
            eventBus.on(ServerEvent.BECOME_SLAVE, (arg) -> reviveThread.setSkipRevive(true));

            reviveThreadList.add(reviveThread);
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


}
