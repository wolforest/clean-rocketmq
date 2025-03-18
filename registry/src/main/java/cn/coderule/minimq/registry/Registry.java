package cn.coderule.minimq.registry;

import cn.coderule.common.convention.service.Lifecycle;

public class Registry implements Lifecycle {
    public static void main(String[] args) {
        new Registry().start();
    }

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
        return null;
    }
}
