package com.wolf.minimq.broker.domain.producer;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.minimq.broker.api.ProducerController;
import com.wolf.minimq.broker.server.vo.BrokerContext;

public class ProducerManager implements Lifecycle {
    private Producer producer;

    @Override
    public void initialize() {
        producer = new Producer();
        producer.initialize();

        ProducerController controller = new ProducerController(producer);
        BrokerContext.registerAPI(controller);
    }

    @Override
    public void start() {
        producer.start();
    }

    @Override
    public void shutdown() {
        producer.shutdown();
    }

    @Override
    public void cleanup() {
        producer.cleanup();
    }

    @Override
    public State getState() {
        return State.RUNNING;
    }
}
