package com.wolf.minimq.broker;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.convention.service.LifecycleManager;
import com.wolf.minimq.broker.server.ComponentRegister;
import com.wolf.minimq.broker.server.ContextInitializer;

/**
 * gateway of broker module
 *  - run()
 */
public class Broker implements Lifecycle {

    public static void main(String[] args) {
        new Broker(args).start();
    }

    private final String[] args;
    private State state = State.INITIALIZING;
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
        this.initialize();
        this.state = State.STARTING;

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
        return state;
    }
}
