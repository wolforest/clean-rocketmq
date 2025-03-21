package cn.coderule.minimq.broker.server;

import cn.coderule.minimq.broker.server.context.BrokerContext;
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
