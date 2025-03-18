package cn.coderule.minimq.registry.domain.broker;

import cn.coderule.common.convention.service.Lifecycle;

public class BrokerManager implements Lifecycle {
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
