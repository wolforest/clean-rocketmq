package cn.coderule.minimq.store;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.utils.lock.StartupLock;
import cn.coderule.minimq.domain.utils.lock.ShutdownLock;
import cn.coderule.minimq.store.server.bootstrap.ContextInitializer;
import cn.coderule.minimq.store.server.bootstrap.ComponentRegister;
import cn.coderule.minimq.store.server.bootstrap.StoreArgument;
import cn.coderule.minimq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.minimq.store.server.StoreContext;
import cn.coderule.minimq.store.server.bootstrap.StorePath;
import cn.coderule.minimq.store.server.bootstrap.StoreScheduler;
import lombok.NonNull;

/**
 * gateway of store module
 *  - start()
 *  - ...
 *  input:
 *   - StoreConfig
 *   - monitor bootstrap
 *  output:
 *   - self
 *   - API bootstrap
 */
public class Store implements Lifecycle {
    private final StoreArgument argument;

    private State state = State.INITIALIZING;
    private LifecycleManager componentManager;

    private StartupLock startupLock;
    private ShutdownLock shutdownLock;

    public Store(@NonNull StoreArgument argument) {
        this.argument = argument;
    }

    public ApplicationContext getAPIContext() {
        return StoreContext.API;
    }

    @Override
    public void initialize() {
        this.argument.validate();
        ContextInitializer.init(argument);

        startupLock = new StartupLock(StorePath.getLockFile());
        shutdownLock = new ShutdownLock(StorePath.getAbortFile());

        this.initScheduler();
        this.initCheckPoint();

        this.componentManager = ComponentRegister.register();
        this.componentManager.initialize();
    }

    @Override
    public void start() {
        this.state = State.STARTING;
        startupLock.lock();

        this.componentManager.start();

        shutdownLock.lock();
        this.state = State.RUNNING;
    }

    @Override
    public void shutdown() {
        this.state = State.SHUTTING_DOWN;

        this.componentManager.shutdown();

        StoreContext.getScheduler().shutdown();
        StoreContext.getCheckPoint().save();

        startupLock.unlock();
        shutdownLock.unlock();

        this.state = State.TERMINATED;
    }

    @Override
    public void cleanup() {
        this.componentManager.cleanup();
    }

    @Override
    public State getState() {
        return this.state;
    }

    private void initScheduler() {
        StoreConfig storeConfig = StoreContext.getBean(StoreConfig.class);
        StoreScheduler scheduler = new StoreScheduler(storeConfig);
        StoreContext.setScheduler(scheduler);
    }

    private void initCheckPoint() {
        boolean isShutdownSuccessful = !shutdownLock.isLocked();

        StoreCheckpoint checkpoint = new StoreCheckpoint(StorePath.getCheckpointPath());
        checkpoint.setShutdownSuccessful(isShutdownSuccessful);

        StoreContext.setCheckPoint(checkpoint);
    }
}
