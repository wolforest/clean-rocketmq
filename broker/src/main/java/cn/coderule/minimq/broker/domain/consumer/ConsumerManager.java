package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;

public class ConsumerManager implements Lifecycle {
    private Consumer consumer;

    @Override
    public void initialize() {
        consumer = new Consumer();

        ConsumerController controller = new ConsumerController(consumer);
        BrokerContext.registerAPI(controller);
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

}
