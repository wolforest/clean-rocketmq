package cn.coderule.minimq.store.server.bootstrap;

import cn.coderule.minimq.domain.config.CommitLogConfig;
import cn.coderule.minimq.domain.config.ConsumeQueueConfig;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.store.server.StoreContext;

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
        initializeConfig();
        initializeMonitor();
    }

    private void initializeConfig() {
        StoreConfig storeConfig = new StoreConfig();
        StorePath.setRootPath(storeConfig.getRootDir());
        StorePath.initPath();

        StoreContext.register(storeConfig);
        StoreContext.register(new MessageConfig());
        StoreContext.register(new CommitLogConfig());
        StoreContext.register(new ConsumeQueueConfig());
        StoreContext.register(new TimerConfig());
    }

    private void initializeMonitor() {
        if (null == argument.getMonitorContext()) {
            return;
        }

        StoreContext.MONITOR = argument.getMonitorContext();
    }

}
