package cn.coderule.minimq.broker.server.bootstrap;

import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.config.GrpcConfig;
import cn.coderule.minimq.domain.config.MessageConfig;

public class ConfigLoader {
    public static void load() {
        BrokerContext.register(new MessageConfig());
        BrokerContext.register(new GrpcConfig());
        BrokerContext.register(new BrokerConfig());
    }
}
