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

        storeConfig.setMessageConfig(new MessageConfig());
        storeConfig.setTopicConfig(new TopicConfig());
        storeConfig.setCommitConfig(new CommitConfig());
        storeConfig.setConsumeQueueConfig(new ConsumeQueueConfig());
        storeConfig.setTimerConfig(new TimerConfig());
        storeConfig.setMetaConfig(new MetaConfig());
        storeConfig.setRpcClientConfig(new RpcClientConfig());

        StorePath.initPath(rootPath);
        return storeConfig;
    }

    public static BrokerConfig createBrokerConfig() {
        BrokerConfig brokerConfig = new BrokerConfig();

        brokerConfig.setMessageConfig(new MessageConfig());
        brokerConfig.setTopicConfig(new TopicConfig());
        brokerConfig.setTimerConfig(new TimerConfig());
        brokerConfig.setTransactionConfig(new TransactionConfig());
        brokerConfig.setTaskConfig(new TaskConfig());

        brokerConfig.setGrpcConfig(new GrpcConfig());
        brokerConfig.setRpcClientConfig(new RpcClientConfig());
        brokerConfig.setRpcServerConfig(new RpcServerConfig());

        return brokerConfig;
    }
}
