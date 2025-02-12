package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.common.convention.service.Lifecycle;

public class Transaction implements Lifecycle {
    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }
}
