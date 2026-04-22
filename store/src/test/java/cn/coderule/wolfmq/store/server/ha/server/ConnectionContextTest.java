package cn.coderule.wolfmq.store.server.ha.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.common.lang.concurrent.thread.WakeupCoordinator;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.domain.store.api.CommitLogStore;
import cn.coderule.wolfmq.store.server.ha.core.HAContext;
import cn.coderule.wolfmq.store.server.ha.core.monitor.FlowMonitor;
import cn.coderule.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import cn.coderule.wolfmq.store.server.ha.server.processor.CommitLogTransfer;
import cn.coderule.wolfmq.store.server.ha.server.processor.SlaveOffsetReceiver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConnectionContextTest {

    @Test
    void testDefaultConstructor() {
        ConnectionContext context = new ConnectionContext();
        assertNull(context.getStoreConfig());
        assertNull(context.getResourcePool());
        assertNull(context.getConnectionPool());
        assertNull(context.getWakeupCoordinator());
        assertNull(context.getFlowMonitor());
        assertNull(context.getSlaveOffsetReceiver());
        assertNull(context.getCommitLogSynchronizer());
        assertNull(context.getCommitLogTransfer());
        assertNull(context.getCommitLogStore());
        assertNull(context.getSocketChannel());
    }

    @Test
    void testBuilder() {
        StoreConfig storeConfig = new StoreConfig();
        LifecycleManager resourcePool = new LifecycleManager();
        ConnectionPool connectionPool = mock(ConnectionPool.class);
        WakeupCoordinator wakeupCoordinator = new WakeupCoordinator();
        FlowMonitor flowMonitor = mock(FlowMonitor.class);
        SlaveOffsetReceiver slaveOffsetReceiver = mock(SlaveOffsetReceiver.class);
        CommitLogSynchronizer commitLogSynchronizer = mock(CommitLogSynchronizer.class);
        CommitLogTransfer commitLogTransfer = mock(CommitLogTransfer.class);
        CommitLogStore commitLogStore = mock(CommitLogStore.class);

        ConnectionContext context = ConnectionContext.builder()
            .storeConfig(storeConfig)
            .resourcePool(resourcePool)
            .connectionPool(connectionPool)
            .wakeupCoordinator(wakeupCoordinator)
            .flowMonitor(flowMonitor)
            .slaveOffsetReceiver(slaveOffsetReceiver)
            .commitLogSynchronizer(commitLogSynchronizer)
            .commitLogTransfer(commitLogTransfer)
            .commitLogStore(commitLogStore)
            .build();

        assertSame(storeConfig, context.getStoreConfig());
        assertSame(resourcePool, context.getResourcePool());
        assertSame(connectionPool, context.getConnectionPool());
        assertSame(wakeupCoordinator, context.getWakeupCoordinator());
        assertSame(flowMonitor, context.getFlowMonitor());
        assertSame(slaveOffsetReceiver, context.getSlaveOffsetReceiver());
        assertSame(commitLogSynchronizer, context.getCommitLogSynchronizer());
        assertSame(commitLogTransfer, context.getCommitLogTransfer());
        assertSame(commitLogStore, context.getCommitLogStore());
    }

    @Test
    void testOfHAContext() {
        StoreConfig storeConfig = new StoreConfig();
        LifecycleManager resourcePool = new LifecycleManager();
        ConnectionPool connectionPool = mock(ConnectionPool.class);
        WakeupCoordinator wakeupCoordinator = new WakeupCoordinator();

        HAContext haContext = HAContext.builder()
            .storeConfig(storeConfig)
            .resourcePool(resourcePool)
            .connectionPool(connectionPool)
            .wakeupCoordinator(wakeupCoordinator)
            .build();

        ConnectionContext context = ConnectionContext.of(haContext);

        assertSame(storeConfig, context.getStoreConfig());
        assertSame(wakeupCoordinator, context.getWakeupCoordinator());
        assertSame(resourcePool, context.getResourcePool());
        assertSame(connectionPool, context.getConnectionPool());
        // Fields not copied from HAContext should be null
        assertNull(context.getFlowMonitor());
        assertNull(context.getSlaveOffsetReceiver());
        assertNull(context.getCommitLogSynchronizer());
        assertNull(context.getCommitLogTransfer());
        assertNull(context.getCommitLogStore());
        assertNull(context.getSocketChannel());
    }
}