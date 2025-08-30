package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.rpc.store.facade.MQFacade;

public class AckManager implements Lifecycle {
    @Override
    public void initialize() throws Exception {
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        MQFacade mqFacade = BrokerContext.getBean(MQStore.class);

        AckService ackService = new AckService(brokerConfig, mqFacade);
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
