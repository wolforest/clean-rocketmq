package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;

public class AckManager implements Lifecycle {
    @Override
    public void initialize() throws Exception {
        AckService ackService = new AckService();
        BrokerContext.register(ackService);

        InvisibleService invisibleService = new InvisibleService();
        BrokerContext.register(invisibleService);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


}
