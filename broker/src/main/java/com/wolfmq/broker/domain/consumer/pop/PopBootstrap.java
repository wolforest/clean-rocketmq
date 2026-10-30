package com.wolfmq.broker.domain.consumer.pop;


import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.broker.domain.consumer.renew.DefaultReceiptHandler;
import com.wolfmq.broker.domain.meta.RouteService;
import com.wolfmq.broker.infra.store.MQStore;
import com.wolfmq.broker.infra.store.SubscriptionStore;
import com.wolfmq.broker.infra.store.TopicStore;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;

public class PopBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public PopBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private BrokerConfig brokerConfig;
    private QueueSelector queueSelector;
    private BrokerDequeueService dequeueService;
    private ContextBuilder contextBuilder;

    @Override
    public void initialize() throws Exception {
        brokerConfig = context.getBean(BrokerConfig.class);

        initContextBuilder();
        initQueueSelector();
        initDequeueService();
        initPopService();
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
    }

    private void initContextBuilder() {
        contextBuilder = new ContextBuilder(
            brokerConfig,
            context.getBean(ConsumerManager.class),
            context.getBean(TopicStore.class),
            context.getBean(SubscriptionStore.class)
        );
    }

    private void initQueueSelector() {
        queueSelector = new QueueSelector(
            brokerConfig,
            context.getBean(RouteService.class)
        );
    }

    private void initDequeueService() {
        dequeueService = new BrokerDequeueService(
            context.getBean(MQStore.class)
        );
    }

    private void initPopService() {
        ReceiptHandler receiptHandler = context.getBean(DefaultReceiptHandler.class);
        PopService popService = new PopService(
            contextBuilder, queueSelector,
            dequeueService, receiptHandler
        );

        context.register(popService);
    }

}
