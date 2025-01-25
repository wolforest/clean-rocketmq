package com.wolf.minimq.broker.domain.producer;

import com.wolf.common.convention.service.Lifecycle;
import java.util.concurrent.ThreadPoolExecutor;

public class Producer implements Lifecycle {
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
