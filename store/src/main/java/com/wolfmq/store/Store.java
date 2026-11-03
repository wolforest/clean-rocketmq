package com.wolfmq.store;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.convention.service.LifecycleManager;
import com.wolfmq.common.core.lock.server.StartupLock;
import com.wolfmq.common.core.lock.server.ShutdownLock;
import com.wolfmq.store.server.bootstrap.ContextInitializer;
import com.wolfmq.store.server.bootstrap.ComponentRegister;
import com.wolfmq.store.server.bootstrap.StoreArgument;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.common.core.constant.ContextNamespace;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.server.store.checkpoint.bakeup.CheckPoint;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * gateway of store module
 *  - main()
 *  - input:
 *   * StoreConfig
 *   * monitor bootstrap
 *  - output:
 *   * self
 *   * API bootstrap
 */
@Slf4j
public class Store implements Lifecycle {

    public static void main(String[] args) throws Exception {
        new Store(args).start();
    }

    private final StoreArgument argument;
    @Getter private final ApplicationContext context;
    private LifecycleManager componentManager;

    private StartupLock startupLock;
    private ShutdownLock shutdownLock;

    public Store(String[] args) {
        this(new StoreArgument(args));
    }

    public Store(@NonNull StoreArgument argument) {
        this.argument = argument;
        this.context = new ApplicationContext(ContextNamespace.APP);
    }

    @Override
    public void initialize() throws Exception {
        ContextInitializer.init(argument, context);

        StoreConfig storeConfig = context.getBean(StoreConfig.class);
        startupLock = new StartupLock(storeConfig.getPath().getLockFile());
        shutdownLock = new ShutdownLock(storeConfig.getPath().getAbortFile());

        this.initCheckPoint(storeConfig);
        this.componentManager = new ComponentRegister(context).register();
        this.componentManager.initialize();
    }

    @Override
    public void start() throws Exception {
        log.info("Store is starting");

        this.initialize();

        startupLock.lock();
        this.componentManager.start();

        log.info("Store start successfully");
    }

    @Override
    public void shutdown() throws Exception {
        log.info("Store is shutting down ...");

        shutdownLock.lock();
        this.componentManager.shutdown();
        context.getBean(CheckPoint.class).save();

        startupLock.unlock();
        shutdownLock.unlock();

        log.info("Store is terminated.");
    }

    @Override
    public void cleanup() throws Exception {
        this.componentManager.cleanup();
    }

    private void initCheckPoint(StoreConfig storeConfig) {
        boolean isShutdownSuccessful = !shutdownLock.isLocked();

        StoreCheckpoint checkpoint = new StoreCheckpoint(storeConfig.getPath().getCheckpointPath());
        checkpoint.setShutdownSuccessful(isShutdownSuccessful);

        context.register(checkpoint, CheckPoint.class);
    }

}
