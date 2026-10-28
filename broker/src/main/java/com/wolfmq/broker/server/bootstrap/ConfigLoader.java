package com.wolfmq.broker.server.bootstrap;

import com.wolfmq.domain.config.business.TimerConfig;
import com.wolfmq.domain.config.business.TransactionConfig;
import com.wolfmq.domain.config.network.RpcClientConfig;
import com.wolfmq.domain.config.network.RpcServerConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.config.network.GrpcConfig;
import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.business.TopicConfig;
import com.wolfmq.domain.config.business.TaskConfig;

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
