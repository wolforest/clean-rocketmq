package cn.coderule.minimq.domain.test;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.business.TaskConfig;
import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.business.TopicConfig;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.config.network.GrpcConfig;
import cn.coderule.minimq.domain.config.network.RpcClientConfig;
import cn.coderule.minimq.domain.config.network.RpcServerConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.minimq.domain.config.store.MetaConfig;
import cn.coderule.minimq.domain.config.store.StorePath;

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
