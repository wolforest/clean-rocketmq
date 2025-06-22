package cn.coderule.minimq.broker.server.bootstrap;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.server.GrpcConfig;
import cn.coderule.minimq.domain.config.message.MessageConfig;
import cn.coderule.minimq.domain.config.message.TopicConfig;

public class ConfigLoader {
    public static void load() {
        BrokerContext.register(new MessageConfig());
        BrokerContext.register(new GrpcConfig());
        BrokerContext.register(new TopicConfig());
        BrokerContext.register(new BrokerConfig());
    }
}
