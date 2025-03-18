package cn.coderule.minimq.registry.domain.kv;

import cn.coderule.common.convention.service.Lifecycle;

public class KVManager implements Lifecycle {
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
