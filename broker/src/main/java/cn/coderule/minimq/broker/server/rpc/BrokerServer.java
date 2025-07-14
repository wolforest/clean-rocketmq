package cn.coderule.minimq.broker.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;

public class BrokerServer implements Lifecycle {
    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void initialize() throws Exception {
        Lifecycle.super.initialize();
    }
}
