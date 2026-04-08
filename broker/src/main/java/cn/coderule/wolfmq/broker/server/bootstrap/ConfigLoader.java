package cn.coderule.wolfmq.broker.server.bootstrap;

import cn.coderule.wolfmq.domain.config.business.TimerConfig;
import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import cn.coderule.wolfmq.domain.config.network.RpcClientConfig;
import cn.coderule.wolfmq.domain.config.network.RpcServerConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.config.network.GrpcConfig;
import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.business.TopicConfig;
import cn.coderule.wolfmq.domain.config.business.TaskConfig;

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
