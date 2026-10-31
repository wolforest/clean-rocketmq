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

public class ConfigLoader {
    public static void load(ApplicationContext context) {
        BrokerConfig brokerConfig = new BrokerConfig();
        brokerConfig.setMessage(new MessageConfig());
        brokerConfig.setTopic(new TopicConfig());
        brokerConfig.setTimer(new TimerConfig());
        brokerConfig.setTransaction(new TransactionConfig());
        brokerConfig.setTask(new TaskConfig());

        brokerConfig.setGrpc(new GrpcConfig());
        brokerConfig.setRpcServer(new RpcServerConfig());
        brokerConfig.setRpcClient(new RpcClientConfig());

        context.register(brokerConfig);
    }
}
