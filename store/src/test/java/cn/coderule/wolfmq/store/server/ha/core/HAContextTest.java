package cn.coderule.wolfmq.store.server.ha.core;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.common.lang.concurrent.thread.WakeupCoordinator;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.store.server.ha.client.HAClient;
import cn.coderule.wolfmq.store.server.ha.server.ConnectionPool;
import cn.coderule.wolfmq.store.server.ha.server.HAServer;
import cn.coderule.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import cn.coderule.wolfmq.store.server.ha.server.processor.SlaveOffsetReceiver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HAContextTest {

    @Test
    void testDefaultConstructor() {
        HAContext context = new HAContext();
        assertNull(context.getStoreConfig());
        assertNull(context.getHaServer());
        assertNull(context.getHaClient());
        assertNull(context.getResourcePool());
        assertNull(context.getConnectionPool());
        assertNull(context.getWakeupCoordinator());
        assertNull(context.getSlaveOffsetReceiver());
        assertNull(context.getCommitLogSynchronizer());
        assertNull(context.getSocketAddress());
        assertNull(context.getServerSocketChannel());
        assertNull(context.getSelector());
    }

    @Test
    void testBuilder() {
        StoreConfig storeConfig = new StoreConfig();
        HAServer haServer = mock(HAServer.class);
        HAClient haClient = mock(HAClient.class);
        LifecycleManager resourcePool = new LifecycleManager();
        ConnectionPool connectionPool = mock(ConnectionPool.class);
        WakeupCoordinator wakeupCoordinator = new WakeupCoordinator();
        SlaveOffsetReceiver slaveOffsetReceiver = mock(SlaveOffsetReceiver.class);
        CommitLogSynchronizer commitLogSynchronizer = mock(CommitLogSynchronizer.class);

        HAContext context = HAContext.builder()
            .storeConfig(storeConfig)
            .haServer(haServer)
            .haClient(haClient)
            .resourcePool(resourcePool)
            .connectionPool(connectionPool)
            .wakeupCoordinator(wakeupCoordinator)
            .slaveOffsetReceiver(slaveOffsetReceiver)
            .commitLogSynchronizer(commitLogSynchronizer)
            .build();

        assertSame(storeConfig, context.getStoreConfig());
        assertSame(haServer, context.getHaServer());
        assertSame(haClient, context.getHaClient());
        assertSame(resourcePool, context.getResourcePool());
        assertSame(connectionPool, context.getConnectionPool());
        assertSame(wakeupCoordinator, context.getWakeupCoordinator());
        assertSame(slaveOffsetReceiver, context.getSlaveOffsetReceiver());
        assertSame(commitLogSynchronizer, context.getCommitLogSynchronizer());
    }

    @Test
    void testSettersAndGetters() {
        HAContext context = new HAContext();
        StoreConfig storeConfig = new StoreConfig();
        context.setStoreConfig(storeConfig);
        assertSame(storeConfig, context.getStoreConfig());
    }

    @Test
    void testBuilderDefaultHaClientIsNull() {
        HAContext context = HAContext.builder()
            .storeConfig(new StoreConfig())
            .build();
        assertNull(context.getHaClient());
    }
}