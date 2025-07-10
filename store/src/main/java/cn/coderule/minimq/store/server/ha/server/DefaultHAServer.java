package cn.coderule.minimq.store.server.ha.server;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.store.server.ha.HAServer;

public class DefaultHAServer extends ServiceThread implements HAServer, Lifecycle {
    @Override
    public String getServiceName() {
        return DefaultHAServer.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
