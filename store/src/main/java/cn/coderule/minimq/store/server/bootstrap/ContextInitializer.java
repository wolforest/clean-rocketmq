package cn.coderule.minimq.store.server.bootstrap;

import cn.coderule.minimq.domain.config.network.RpcClientConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.business.TopicConfig;
import cn.coderule.minimq.domain.config.store.MetaConfig;

public class ContextInitializer {
    private final StoreArgument argument;

    public static void init(StoreArgument argument) {
        ContextInitializer initializer = new ContextInitializer(argument);
        initializer.initialize();
    }

    public ContextInitializer(StoreArgument argument) {
        this.argument = argument;
    }

    public void initialize() {
        this.argument.validate();

        initializeConfig();
        initializeMonitor();
    }

    private void initializeConfig() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setMessageConfig(new MessageConfig());
        storeConfig.setTopicConfig(new TopicConfig());
        storeConfig.setCommitConfig(new CommitConfig());
        storeConfig.setConsumeQueueConfig(new ConsumeQueueConfig());
        storeConfig.setTimerConfig(new TimerConfig());
        storeConfig.setMetaConfig(new MetaConfig());
        storeConfig.setRpcClientConfig(new RpcClientConfig());

        StorePath.setRootPath(storeConfig.getRootDir());
        StorePath.initPath();

        StoreContext.register(storeConfig);
    }

    private void initializeMonitor() {
        if (null == argument.getMonitorContext()) {
            return;
        }

        StoreContext.MONITOR = argument.getMonitorContext();
    }

}
