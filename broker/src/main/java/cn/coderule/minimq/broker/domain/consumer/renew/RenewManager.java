package cn.coderule.minimq.broker.domain.consumer.renew;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class RenewManager implements Lifecycle {
    private BrokerConfig brokerConfig;

    @Override
    public void initialize() throws Exception {
        brokerConfig = BrokerContext.getBean(BrokerConfig.class);
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


}
