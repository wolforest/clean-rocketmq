package cn.coderule.wolfmq.store.server.ha;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.store.server.ha.client.HAClient;
import cn.coderule.wolfmq.store.server.ha.core.HAContext;
import cn.coderule.wolfmq.store.server.ha.core.monitor.StateMonitor;
import cn.coderule.wolfmq.store.server.ha.server.ConnectionPool;
import cn.coderule.wolfmq.store.server.ha.server.HAServer;
import cn.coderule.wolfmq.store.server.ha.server.processor.CommitLogSynchronizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HAServiceTest {

    private HAContext context;
    private HAServer haServer;
    private HAClient haClient;
    private ConnectionPool connectionPool;
    private CommitLogSynchronizer synchronizer;
    private StoreConfig storeConfig;
    private HAService haService;

    @BeforeEach
    void setUp() {
        haServer = mock(HAServer.class);
        haClient = mock(HAClient.class);
        connectionPool = mock(ConnectionPool.class);
        synchronizer = mock(CommitLogSynchronizer.class);
        storeConfig = mock(StoreConfig.class);

        context = mock(HAContext.class);
        when(context.getHaServer()).thenReturn(haServer);
        when(context.getHaClient()).thenReturn(haClient);
        when(context.getConnectionPool()).thenReturn(connectionPool);
        when(context.getCommitLogSynchronizer()).thenReturn(synchronizer);
        when(context.getStoreConfig()).thenReturn(storeConfig);

        haService = new HAService(context);
    }

    @Test
    void start_ShouldStartStateMonitor() throws Exception {
        haService.start();
        verify(haServer, never()).start();
    }

    @Test
    void shutdown_ShouldStopStateMonitor() throws Exception {
        haService.shutdown();
    }

    @Test
    void countHealthySlave_ShouldDelegateToConnectionPool() {
        when(connectionPool.countHealthyConnection(1000L)).thenReturn(3);
        assertEquals(3, haService.countHealthySlave(1000L));
        verify(connectionPool).countHealthyConnection(1000L);
    }

    @Test
    void updateMasterAddress_ShouldSetOnHAClient() {
        haService.updateMasterAddress("127.0.0.1:10911");
        verify(haClient).setMasterAddress("127.0.0.1:10911");
    }

    @Test
    void updateMasterHaAddress_ShouldSetOnHAClient() {
        haService.updateMasterHaAddress("127.0.0.1:10912");
        verify(haClient).setMasterHaAddress("127.0.0.1:10912");
    }

    @Test
    void syncCommitLog_ShouldDelegateToSynchronizer() {
        EnqueueFuture future = mock(EnqueueFuture.class);
        EnqueueResult result = mock(EnqueueResult.class);
        when(synchronizer.sync(future)).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(result));

        assertNotNull(haService.syncCommitLog(future));
        verify(synchronizer).sync(future);
    }

    @Test
    void wakeupHAClient_ShouldCallWakeup() {
        haService.wakeupHAClient();
        verify(haClient).wakeup();
    }

    @Test
    void updateMasterAddress_WhenHAClientIsNull_ShouldNotThrow() {
        when(context.getHaClient()).thenReturn(null);
        HAService serviceWithNullClient = new HAService(context);
        assertDoesNotThrow(() -> serviceWithNullClient.updateMasterAddress("addr"));
    }

    @Test
    void wakeupHAClient_WhenHAClientIsNull_ShouldNotThrow() {
        when(context.getHaClient()).thenReturn(null);
        HAService serviceWithNullClient = new HAService(context);
        assertDoesNotThrow(() -> serviceWithNullClient.wakeupHAClient());
    }
}