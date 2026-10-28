package com.wolfmq.store;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.convention.service.LifecycleManager;
import com.wolfmq.domain.core.lock.server.StartupLock;
import com.wolfmq.domain.core.lock.server.ShutdownLock;
import com.wolfmq.store.server.bootstrap.ContextInitializer;
import com.wolfmq.store.server.bootstrap.ComponentRegister;
import com.wolfmq.store.server.bootstrap.StoreArgument;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;
import com.wolfmq.store.server.bootstrap.StoreContext;
import com.wolfmq.domain.config.store.StorePath;
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
    public static void main(String[] args) {
        Store store = new Store(args);
        try {
            store.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            store.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final StoreArgument argument;
    private LifecycleManager componentManager;

    private StartupLock startupLock;
    private ShutdownLock shutdownLock;

    public Store(String[] args) {
        this(new StoreArgument(args));
    }

    public Store(@NonNull StoreArgument argument) {
        this.argument = argument;
    }

    @Override
    public void initialize() throws Exception {
        ContextInitializer.init(argument);

        startupLock = new StartupLock(StorePath.getLockFile());
        shutdownLock = new ShutdownLock(StorePath.getAbortFile());

        this.initCheckPoint();
        this.componentManager = ComponentRegister.register();
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
        StoreContext.getCheckPoint().save();

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

        StoreContext.setCheckPoint(checkpoint);
    }
}
