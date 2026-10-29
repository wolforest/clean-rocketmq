package com.wolfmq.broker.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.config.business.TimerConfig;
import com.wolfmq.domain.config.business.TransactionConfig;
import com.wolfmq.domain.config.network.RpcClientConfig;
import com.wolfmq.domain.config.network.RpcServerConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.config.network.GrpcConfig;
import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.business.TopicConfig;
import com.wolfmq.domain.config.business.TaskConfig;
import com.wolfmq.domain.core.constant.ContextNamespace;

public class ConfigLoader {
    public static void load(ApplicationContext context) {
        BrokerConfig brokerConfig = new BrokerConfig();
        brokerConfig.setMessageConfig(new MessageConfig());
        brokerConfig.setTopicConfig(new TopicConfig());
        brokerConfig.setTimerConfig(new TimerConfig());
        brokerConfig.setTransactionConfig(new TransactionConfig());
        brokerConfig.setTaskConfig(new TaskConfig());

        brokerConfig.setGrpcConfig(new GrpcConfig());
        brokerConfig.setRpcServerConfig(new RpcServerConfig());
        brokerConfig.setRpcClientConfig(new RpcClientConfig());

        context.register(brokerConfig, ContextNamespace.APP);
    }
}
