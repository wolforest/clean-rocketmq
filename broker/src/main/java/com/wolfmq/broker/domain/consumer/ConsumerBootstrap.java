package com.wolfmq.broker.domain.consumer;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.broker.api.ConsumerController;
import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.broker.domain.consumer.ack.AckBootstrap;
import com.wolfmq.broker.domain.consumer.ack.BrokerAckService;
import com.wolfmq.broker.domain.consumer.ack.InvisibleService;
import com.wolfmq.broker.domain.consumer.consumer.ConsumeHookManager;
import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.broker.domain.consumer.pop.PopBootstrap;
import com.wolfmq.broker.domain.consumer.pop.PopService;
import com.wolfmq.broker.domain.consumer.renew.RenewBootstrap;
import com.wolfmq.broker.domain.consumer.revive.ReviveBootstrap;
import com.wolfmq.broker.infra.store.SubscriptionStore;
import com.wolfmq.domain.config.server.BrokerConfig;

public class ConsumerBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public ConsumerBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private BrokerConfig brokerConfig;
    private Consumer consumer;

    private PopBootstrap popBootstrap;
    private AckBootstrap ackBootstrap;
    private RenewBootstrap renewBootstrap;
    private ReviveBootstrap reviveBootstrap;

    @Override
    public void initialize() throws Exception {
        brokerConfig = context.getBean(BrokerConfig.class, ContextNamespace.APP);
        initTools();

        initRenew();
        initAck();
        initPop();
        initRevive();

        initConsumer();
        initController();
    }

    @Override
    public void start() throws Exception {
        renewBootstrap.start();
        ackBootstrap.start();
        popBootstrap.start();
        reviveBootstrap.start();
    }

    @Override
    public void shutdown() throws Exception {
        popBootstrap.shutdown();
        ackBootstrap.shutdown();
        reviveBootstrap.shutdown();
        renewBootstrap.shutdown();
    }

    private void initTools() {
        ConsumerManager register = new ConsumerManager(brokerConfig);
        context.register(register, ContextNamespace.APP);

        ConsumeHookManager hookManager = new ConsumeHookManager();
        context.register(hookManager, ContextNamespace.APP);
    }

    private void initRenew() throws Exception {
        renewBootstrap = new RenewBootstrap(context);
        renewBootstrap.initialize();
    }

    private void initAck() throws Exception {
        ackBootstrap = new AckBootstrap(context);
        ackBootstrap.initialize();
    }

    private void initPop() throws Exception {
        popBootstrap = new PopBootstrap(context);
        popBootstrap.initialize();
    }

    private void initRevive() throws Exception {
        reviveBootstrap = new ReviveBootstrap(context);
        reviveBootstrap.initialize();
    }

    private void initConsumer() {
        consumer = new Consumer(
            context.getBean(PopService.class, ContextNamespace.APP),
            context.getBean(BrokerAckService.class, ContextNamespace.APP),
            context.getBean(ConsumerManager.class, ContextNamespace.APP),
            context.getBean(InvisibleService.class, ContextNamespace.APP),
            context.getBean(SubscriptionStore.class, ContextNamespace.APP)
        );
        context.register(consumer, ContextNamespace.APP);
    }

    private void initController() {
        ConsumerController controller = new ConsumerController(brokerConfig, consumer);
        context.register(controller, ContextNamespace.API);
    }

}
