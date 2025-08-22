package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.ack.AckManager;
import cn.coderule.minimq.broker.domain.consumer.ack.AckService;
import cn.coderule.minimq.broker.domain.consumer.ack.InvisibleService;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumeHookManager;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.domain.consumer.consumer.InflightCounter;
import cn.coderule.minimq.broker.domain.consumer.pop.PopManager;
import cn.coderule.minimq.broker.domain.consumer.pop.PopService;
import cn.coderule.minimq.broker.domain.consumer.revive.ReviveManager;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class ConsumerManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private Consumer consumer;

    private PopManager popManager;
    private AckManager ackManager;
    private ReviveManager reviveManager;

    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);

        initTools();

        initAck();
        initPop();
        initRevive();

        initConsumer();
        initController();
    }

    @Override
    public void start() throws Exception {
        ackManager.start();
        popManager.start();
        reviveManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        ackManager.shutdown();
        popManager.shutdown();
        reviveManager.shutdown();
    }

    private void initAck() throws Exception {
        ackManager = new AckManager();
        ackManager.initialize();
    }

    private void initPop() throws Exception {
        popManager = new PopManager();
        popManager.initialize();
    }

    private void initRevive() throws Exception {
        reviveManager = new ReviveManager();
        reviveManager.initialize();
    }

    private void initConsumer() {
        consumer = new Consumer(
            BrokerContext.getBean(PopService.class),
            BrokerContext.getBean(AckService.class),
            BrokerContext.getBean(ConsumerRegister.class),
            BrokerContext.getBean(InvisibleService.class),
            BrokerContext.getBean(SubscriptionStore.class)
        );
        BrokerContext.register(consumer);
    }

    private void initController() {
        ConsumerController controller = new ConsumerController(brokerConfig, consumer);
        BrokerContext.registerAPI(controller);
    }

    private void initTools() {
        ConsumerRegister register = new ConsumerRegister(brokerConfig);
        BrokerContext.register(register);

        InflightCounter inflightCounter = new InflightCounter();
        BrokerContext.register(inflightCounter);

        ConsumeHookManager hookManager = new ConsumeHookManager();
        BrokerContext.register(hookManager);
    }



}
