package com.wolf.minimq.broker.server;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.convention.service.LifecycleManager;

/**
 * gateway of broker module
 *  - run()
 */
public class Broker implements Lifecycle {
    private State state = State.INITIALIZING;
    private final String[] args;

    private LifecycleManager componentManager;

    public Broker(String[] args) {
        this.args = args;
    }

    @Override
    public void initialize() {
        ContextInitializer.init(args);
        this.componentManager = ComponentRegister.register();
    }

    @Override
    public void start() {
        this.state = State.STARTING;
        this.initialize();

        this.componentManager.start();

        this.state = State.RUNNING;
    }

    @Override
    public void shutdown() {
        this.state = State.SHUTTING_DOWN;
        this.cleanup();

        this.componentManager.shutdown();

        this.state = State.TERMINATED;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return null;
    }
}
