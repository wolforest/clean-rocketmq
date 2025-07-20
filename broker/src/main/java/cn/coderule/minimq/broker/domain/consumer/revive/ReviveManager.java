package cn.coderule.minimq.broker.domain.consumer.revive;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.store.facade.ConsumeOffsetFacade;
import cn.coderule.minimq.rpc.store.facade.SubscriptionFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;
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
    public void initialize() throws Exception {
        ReviveContext context = initContext();
        RetryService retryService = new RetryService(context);
        int queueNum = context.getTopicConfig().getReviveQueueNum();

        for (int i = 0; i < queueNum; i++) {
            ReviveThread task = createReviveThread(context, i, retryService);
            reviveThreadList.add(task);
        }
    }

    @Override
    public void start() throws Exception {
        for (ReviveThread reviveThread : reviveThreadList) {
            reviveThread.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        for (ReviveThread reviveThread : reviveThreadList) {
            reviveThread.shutdown();
        }
    }

    private ReviveContext initContext() {
        BrokerConfig brokerConfig = StoreContext.getBean(BrokerConfig.class);
        String reviveTopic = KeyBuilder.buildClusterReviveTopic(brokerConfig.getCluster());

        return ReviveContext.builder()
            .reviveTopic(reviveTopic)
            .brokerConfig(brokerConfig)
            .messageConfig(brokerConfig.getMessageConfig())
            .topicConfig(brokerConfig.getTopicConfig())

            .mqFacade(StoreContext.getBean(MQFacade.class))

            .topicFacade(StoreContext.getBean(TopicFacade.class))
            .subscriptionFacade(StoreContext.getBean(SubscriptionFacade.class))
            .consumeOffsetFacade(StoreContext.getBean(ConsumeOffsetFacade.class))

            /* to be deleted */
            .mqService(StoreContext.getBean(MQService.class))
            .consumeQueueGateway(StoreContext.getBean(ConsumeQueueGateway.class))

            .topicService(StoreContext.getBean(TopicService.class))
            .subscriptionService(StoreContext.getBean(SubscriptionService.class))
            .consumeOffsetService(StoreContext.getBean(ConsumeOffsetService.class))
            /* to be deleted */

            .build();
    }

    private ReviveThread createReviveThread(ReviveContext context, int queueId, RetryService retryService) {
        ReviveThread reviveThread = new ReviveThread(context, queueId, retryService);

        ServerEventBus eventBus = StoreContext.getBean(ServerEventBus.class);
        eventBus.on(ServerEvent.BECOME_MASTER, (arg) -> reviveThread.setSkipRevive(false));
        eventBus.on(ServerEvent.BECOME_SLAVE, (arg) -> reviveThread.setSkipRevive(true));

        return reviveThread;
    }


}
