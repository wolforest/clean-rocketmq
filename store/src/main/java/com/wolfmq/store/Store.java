package com.wolfmq.store;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.convention.service.LifecycleManager;
import com.wolfmq.domain.core.lock.server.StartupLock;
import com.wolfmq.domain.core.lock.server.ShutdownLock;
import com.wolfmq.store.server.bootstrap.ContextInitializer;
import com.wolfmq.store.server.bootstrap.ComponentRegister;
import com.wolfmq.store.server.bootstrap.StoreArgument;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.CheckPoint;
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
        Store store = new Store(args);
        store.initialize();
        store.start();
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
        this.context = new ApplicationContext();
    }

    @Override
    public void initialize() throws Exception {
        ContextInitializer.init(argument, context);

        startupLock = new StartupLock(StorePath.getLockFile());
        shutdownLock = new ShutdownLock(StorePath.getAbortFile());

        this.initCheckPoint();
        this.componentManager = new ComponentRegister(context).register();
        this.componentManager.initialize();
    }

    @Override
    public void start() throws Exception {
        log.info("Store is starting");

        startupLock.lock();
        this.componentManager.start();
        shutdownLock.lock();

        log.info("Store start successfully");
    }

    @Override
    public void shutdown() throws Exception {
        log.info("Store is shutting down ...");

        this.componentManager.shutdown();
        context.getBean(CheckPoint.class, ContextNamespace.APP).save();

        startupLock.unlock();
        shutdownLock.unlock();

        log.info("Store is terminated.");
    }

    @Override
    public void cleanup() throws Exception {
        this.componentManager.cleanup();
    }

    private void initCheckPoint() {
        boolean isShutdownSuccessful = !shutdownLock.isLocked();

        StoreCheckpoint checkpoint = new StoreCheckpoint(StorePath.getCheckpointPath());
        checkpoint.setShutdownSuccessful(isShutdownSuccessful);

        context.register(checkpoint, CheckPoint.class, ContextNamespace.APP);
        context.register(checkpoint, StoreCheckpoint.class, ContextNamespace.APP);
    }

}
