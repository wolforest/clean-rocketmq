package com.wolf.minimq.broker.domain.consumer;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.minimq.broker.api.ConsumerController;
import com.wolf.minimq.broker.server.model.BrokerContext;

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
