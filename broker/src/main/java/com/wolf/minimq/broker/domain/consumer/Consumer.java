package com.wolf.minimq.broker.domain.consumer;

import com.wolf.common.convention.service.Lifecycle;
import java.util.concurrent.ThreadPoolExecutor;

public class Consumer implements Lifecycle {
    private ThreadPoolExecutor executor;

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }
}
