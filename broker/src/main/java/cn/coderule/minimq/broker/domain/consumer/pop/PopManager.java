package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;

public class PopManager implements Lifecycle {
    @Override
    public void initialize() throws Exception {
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        String reviveTopic = KeyBuilder.buildClusterReviveTopic(brokerConfig.getCluster());


    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }


}
