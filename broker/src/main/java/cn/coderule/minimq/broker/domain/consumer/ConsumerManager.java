package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.ack.AckManager;
import cn.coderule.minimq.broker.domain.consumer.ack.AckService;
import cn.coderule.minimq.broker.domain.consumer.ack.InvisibleService;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumeHookManager;
import cn.coderule.minimq.broker.domain.consumer.consumer.Consumer;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.domain.consumer.pop.PopManager;
import cn.coderule.minimq.broker.domain.consumer.pop.PopService;
import cn.coderule.minimq.broker.domain.consumer.renew.RenewManager;
import cn.coderule.minimq.broker.domain.consumer.revive.ReviveManager;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class ConsumerManager implements Lifecycle {
    private BrokerConfig brokerConfig;
    private Consumer consumer;

    private PopManager popManager;
    private AckManager ackManager;
    private RenewManager renewManager;
    private ReviveManager reviveManager;

    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);
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
        renewManager.start();
        ackManager.start();
        popManager.start();
        reviveManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        popManager.shutdown();
        ackManager.shutdown();
        reviveManager.shutdown();
        renewManager.shutdown();
    }

    private void initTools() {
        ConsumerRegister register = new ConsumerRegister(brokerConfig);
        BrokerContext.register(register);

        ConsumeHookManager hookManager = new ConsumeHookManager();
        BrokerContext.register(hookManager);
    }

    private void initRenew() throws Exception {
        renewManager = new RenewManager();
        renewManager.initialize();
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

}
