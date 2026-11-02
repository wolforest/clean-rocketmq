package com.wolfmq.broker.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.common.config.business.TimerConfig;
import com.wolfmq.common.config.business.TransactionConfig;
import com.wolfmq.common.config.network.RpcClientConfig;
import com.wolfmq.common.config.network.RpcServerConfig;
import com.wolfmq.common.config.server.BrokerConfig;
import com.wolfmq.common.config.network.GrpcConfig;
import com.wolfmq.common.config.business.MessageConfig;
import com.wolfmq.common.config.business.TopicConfig;
import com.wolfmq.common.config.business.TaskConfig;

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
