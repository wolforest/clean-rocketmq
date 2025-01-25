package com.wolf.minimq.broker.domain.transaction;

import com.wolf.common.convention.service.Lifecycle;

public class TransactionManager implements Lifecycle {
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
