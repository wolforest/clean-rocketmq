package cn.coderule.minimq.store.server.ha.server;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.lang.concurrent.thread.WakeupCoordinator;
import cn.coderule.minimq.store.server.ha.HAConnection;
import cn.coderule.minimq.store.server.ha.HAServer;
import cn.coderule.minimq.store.server.ha.server.socket.ConnectionPool;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultHAServer extends ServiceThread implements HAServer, Lifecycle {
    private final ConnectionPool connectionPool = new ConnectionPool();
    private final WakeupCoordinator wakeupCoordinator = new WakeupCoordinator();
    private AtomicLong pushedOffset = new AtomicLong(0);


    @Override
    public String getServiceName() {
        return DefaultHAServer.class.getSimpleName();
    }

    @Override
    public void run() {

    }

    @Override
    public List<HAConnection> getConnectionList() {
        return connectionPool.getConnectionList();
    }
}
