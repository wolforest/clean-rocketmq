package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.context.BrokerContext;

public class ConsumerManager implements Lifecycle {
    private Consumer consumer;

    @Override
    public void initialize() {
        consumer = new Consumer();
        consumer.initialize();

        ConsumerController controller = new ConsumerController(consumer);
        BrokerContext.registerAPI(controller);
    }

    @Override
    public void start() {
        consumer.start();
    }

    @Override
    public void shutdown() {
        consumer.shutdown();
    }

    @Override
    public void cleanup() {
        consumer.cleanup();
    }

    @Override
    public State getState() {
        return State.RUNNING;
    }
}
