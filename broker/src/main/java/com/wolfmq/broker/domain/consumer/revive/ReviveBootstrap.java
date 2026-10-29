package com.wolfmq.broker.domain.consumer.revive;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.container.EventBus;
import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.infra.store.ConsumeOffsetStore;
import com.wolfmq.broker.infra.store.MQStore;
import com.wolfmq.broker.infra.store.SubscriptionStore;
import com.wolfmq.broker.infra.store.TopicStore;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.meta.topic.KeyBuilder;
import com.wolfmq.domain.core.event.ServerEvent;
import java.util.ArrayList;
import java.util.List;

public class ReviveBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public ReviveBootstrap(ApplicationContext context) {
        this.context = context;
    }

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
        BrokerConfig brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);
        String reviveTopic = KeyBuilder.buildClusterReviveTopic(brokerConfig.getCluster());

        return ReviveContext.builder()
            .reviveTopic(reviveTopic)
            .brokerConfig(brokerConfig)
            .messageConfig(brokerConfig.getMessageConfig())
            .topicConfig(brokerConfig.getTopicConfig())

            .mqFacade(context.getBean(MQStore.class, ContextNamespace.APP))

            .topicFacade(context.getBean(TopicStore.class, ContextNamespace.APP))
            .subscriptionFacade(context.getBean(SubscriptionStore.class, ContextNamespace.APP))
            .consumeOffsetFacade(context.getBean(ConsumeOffsetStore.class, ContextNamespace.APP))

            .build();
    }

    private ReviveThread createReviveThread(ReviveContext reviveContext, int queueId, RetryService retryService) {
        ReviveThread reviveThread = new ReviveThread(reviveContext, queueId, retryService);

        EventBus eventBus = context.getBean(EventBus.class, ContextNamespace.APP);
        eventBus.on(
            ServerEvent.BECOME_MASTER, (arg) -> reviveThread.setSkipRevive(false)
        );
        eventBus.on(
            ServerEvent.BECOME_SLAVE, (arg) -> reviveThread.setSkipRevive(true)
        );

        return reviveThread;
    }


}
