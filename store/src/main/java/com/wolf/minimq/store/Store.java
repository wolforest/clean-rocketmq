package com.wolf.minimq.store;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.convention.service.LifecycleManager;
import com.wolf.minimq.domain.utils.lock.StartupLock;
import com.wolf.minimq.domain.utils.lock.ShutdownLock;
import com.wolf.minimq.store.server.APIRegister;
import com.wolf.minimq.store.server.ContextInitializer;
import com.wolf.minimq.store.server.ComponentRegister;
import com.wolf.minimq.store.server.StoreArgument;
import com.wolf.minimq.store.server.StoreCheckpoint;
import com.wolf.minimq.store.server.StoreContext;
import com.wolf.minimq.store.server.StorePath;
import lombok.NonNull;

/**
 * gateway of store module
 *  - start()
 *
 *  input:
 *   - StoreConfig
 *   - monitor context
 *  output:
 *   - self
 *   - API context
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

    @Override
    public void initialize() {
        this.argument.validate();
        ContextInitializer.init(argument);

        startupLock = new StartupLock(StorePath.getLockFile());
        shutdownLock = new ShutdownLock(StorePath.getAbortFile());

        this.componentManager = ComponentRegister.register();
        this.initCheckPoint();

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
        this.cleanup();

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

    private void initCheckPoint() {
        boolean lastExitOk = !shutdownLock.isLocked();
        StoreCheckpoint checkpoint = new StoreCheckpoint(StorePath.getCheckpointPath());
        checkpoint.setNormalExit(lastExitOk);
        StoreContext.CHECK_POINT = checkpoint;
    }
}
