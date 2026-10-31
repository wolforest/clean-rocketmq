package com.wolfmq.domain.test;

import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.business.TaskConfig;
import com.wolfmq.domain.config.business.TimerConfig;
import com.wolfmq.domain.config.business.TopicConfig;
import com.wolfmq.domain.config.business.TransactionConfig;
import com.wolfmq.domain.config.network.GrpcConfig;
import com.wolfmq.domain.config.network.RpcClientConfig;
import com.wolfmq.domain.config.network.RpcServerConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.config.store.ConsumeQueueConfig;
import com.wolfmq.domain.config.store.MetaConfig;
import com.wolfmq.domain.config.store.StorePath;

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
