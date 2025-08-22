package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.ack.AckManager;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumeHookManager;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.domain.consumer.consumer.InflightCounter;
import cn.coderule.minimq.broker.domain.consumer.pop.PopManager;
import cn.coderule.minimq.broker.domain.consumer.revive.ReviveManager;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class ConsumerManager implements Lifecycle {
    private BrokerConfig brokerConfig;

    private ConsumerRegister register;
    private ConsumeHookManager hookManager;
    private InflightCounter inflightCounter;

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


        Consumer consumer = new Consumer();

        ConsumerController controller = new ConsumerController(brokerConfig, consumer);
        BrokerContext.registerAPI(controller);
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
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

    private void initTools() {
        register = new ConsumerRegister(brokerConfig);
        BrokerContext.register(register);

        inflightCounter = new InflightCounter();
        BrokerContext.register(inflightCounter);

        hookManager = new ConsumeHookManager();
        BrokerContext.register(hookManager);
    }



}
