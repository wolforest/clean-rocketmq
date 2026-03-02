package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.ack.AckBootstrap;
import cn.coderule.minimq.broker.domain.consumer.ack.AckService;
import cn.coderule.minimq.broker.domain.consumer.ack.InvisibleService;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumeHookManager;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegistry;
import cn.coderule.minimq.broker.domain.consumer.pop.PopBootstrap;
import cn.coderule.minimq.broker.domain.consumer.pop.PopService;
import cn.coderule.minimq.broker.domain.consumer.renew.RenewBootstrap;
import cn.coderule.minimq.broker.domain.consumer.revive.ReviveBootstrap;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class ConsumerBootstrap implements Lifecycle {
    private BrokerConfig brokerConfig;
    private Consumer consumer;

    private PopBootstrap popBootstrap;
    private AckBootstrap ackBootstrap;
    private RenewBootstrap renewBootstrap;
    private ReviveBootstrap reviveBootstrap;

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
        ConsumerRegistry register = new ConsumerRegistry(brokerConfig);
        BrokerContext.register(register);

        ConsumeHookManager hookManager = new ConsumeHookManager();
        BrokerContext.register(hookManager);
    }

    private void initRenew() throws Exception {
        renewBootstrap = new RenewBootstrap();
        renewBootstrap.initialize();
    }

    private void initAck() throws Exception {
        ackBootstrap = new AckBootstrap();
        ackBootstrap.initialize();
    }

    private void initPop() throws Exception {
        popBootstrap = new PopBootstrap();
        popBootstrap.initialize();
    }

    private void initRevive() throws Exception {
        reviveBootstrap = new ReviveBootstrap();
        reviveBootstrap.initialize();
    }

    private void initConsumer() {
        consumer = new Consumer(
            BrokerContext.getBean(PopService.class),
            BrokerContext.getBean(AckService.class),
            BrokerContext.getBean(ConsumerRegistry.class),
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
