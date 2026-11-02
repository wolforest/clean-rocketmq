package com.wolfmq.common.test;

import com.wolfmq.common.config.business.MessageConfig;
import com.wolfmq.common.config.business.TaskConfig;
import com.wolfmq.common.config.business.TimerConfig;
import com.wolfmq.common.config.business.TopicConfig;
import com.wolfmq.common.config.business.TransactionConfig;
import com.wolfmq.common.config.network.GrpcConfig;
import com.wolfmq.common.config.network.RpcClientConfig;
import com.wolfmq.common.config.network.RpcServerConfig;
import com.wolfmq.common.config.server.BrokerConfig;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.config.store.ConsumeQueueConfig;
import com.wolfmq.common.config.store.MetaConfig;
import com.wolfmq.common.config.store.StorePath;

public class ConfigMock {
    public static StoreConfig createStoreConfig(String rootPath) {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setRootDir(rootPath);

        storeConfig.setMessage(new MessageConfig());
        storeConfig.setTopic(new TopicConfig());
        storeConfig.setCommit(new CommitConfig());
        storeConfig.setConsumeQueue(new ConsumeQueueConfig());
        storeConfig.setTimer(new TimerConfig());
        storeConfig.setMeta(new MetaConfig());
        storeConfig.setRpcClient(new RpcClientConfig());

        StorePath.initPath(rootPath);
        return storeConfig;
    }

    public static BrokerConfig createBrokerConfig() {
        BrokerConfig brokerConfig = new BrokerConfig();

        brokerConfig.setMessage(new MessageConfig());
        brokerConfig.setTopic(new TopicConfig());
        brokerConfig.setTimer(new TimerConfig());
        brokerConfig.setTransaction(new TransactionConfig());
        brokerConfig.setTask(new TaskConfig());

        brokerConfig.setGrpc(new GrpcConfig());
        brokerConfig.setRpcClient(new RpcClientConfig());
        brokerConfig.setRpcServer(new RpcServerConfig());

        return brokerConfig;
    }
}
