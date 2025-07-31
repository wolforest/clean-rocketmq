package cn.coderule.minimq.broker.server.bootstrap;

import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.TransactionConfig;
import cn.coderule.minimq.domain.config.network.RpcClientConfig;
import cn.coderule.minimq.domain.config.network.RpcServerConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.network.GrpcConfig;
import cn.coderule.minimq.domain.config.message.MessageConfig;
import cn.coderule.minimq.domain.config.message.TopicConfig;
import cn.coderule.minimq.domain.config.server.TaskConfig;

public class ConfigLoader {
    public static void load() {
        BrokerConfig brokerConfig = new BrokerConfig();
        brokerConfig.setMessageConfig(new MessageConfig());
        brokerConfig.setTopicConfig(new TopicConfig());
        brokerConfig.setTimerConfig(new TimerConfig());
        brokerConfig.setTransactionConfig(new TransactionConfig());
        brokerConfig.setTaskConfig(new TaskConfig());

        brokerConfig.setGrpcConfig(new GrpcConfig());
        brokerConfig.setRpcServerConfig(new RpcServerConfig());
        brokerConfig.setRpcClientConfig(new RpcClientConfig());

        BrokerContext.register(brokerConfig);
    }
}
