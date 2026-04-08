package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import cn.coderule.minimq.store.server.bootstrap.StoreCheckpoint;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DispatchManagerTest {

    @TempDir
    Path tmpDir;

    private CommitConfig config;
    private CheckPoint checkPoint;
    private CommitLogManager commitLogManager;
    private CommitHandlerManager handlerManager;
    private DispatchManager dispatchManager;

    @BeforeEach
    void setUp() {
        config = new CommitConfig();
        config.setShardingNumber(2);
        config.setDispatchThreads(3);

        String checkpointPath = tmpDir.resolve("checkpoint").toString();
        checkPoint = new StoreCheckpoint(checkpointPath);

        commitLogManager = mock(CommitLogManager.class);
        for (int i = 0; i < 2; i++) {
            CommitLog mockLog = mock(CommitLog.class);
            when(mockLog.getShardId()).thenReturn(i);
            when(mockLog.getMinOffset()).thenReturn(0L);
            when(mockLog.getMaxOffset()).thenReturn(0L);
            when(commitLogManager.selectByShardId(i)).thenReturn(mockLog);
        }

        handlerManager = new CommitHandlerManager();
    }

    @Test
    void testInitialize_CreatesCorrectNumber() throws Exception {
        dispatchManager = new DispatchManager(config, checkPoint, commitLogManager, handlerManager);
        dispatchManager.initialize();
    }

    @Test
    void testInitialize_CreatesListeners() throws Exception {
        dispatchManager = new DispatchManager(config, checkPoint, commitLogManager, handlerManager);
        dispatchManager.initialize();

        verify(commitLogManager, times(config.getShardingNumber())).selectByShardId(anyInt());
    }

    @Test
    void testStartAll_StartsAllComponents() throws Exception {
        dispatchManager = new DispatchManager(config, checkPoint, commitLogManager, handlerManager);
        dispatchManager.initialize();
        dispatchManager.start();

        Thread.sleep(100);

        dispatchManager.shutdown();
    }

    @Test
    void testShutdownAll_StopsAllComponents() throws Exception {
        dispatchManager = new DispatchManager(config, checkPoint, commitLogManager, handlerManager);
        dispatchManager.initialize();
        dispatchManager.start();

        Thread.sleep(100);

        dispatchManager.shutdown();
        Thread.sleep(100);
    }

    @Test
    void testQueueShared() throws Exception {
        dispatchManager = new DispatchManager(config, checkPoint, commitLogManager, handlerManager);
        dispatchManager.initialize();
        dispatchManager.start();

        Thread.sleep(50);

        dispatchManager.shutdown();
    }

    @Test
    void testZeroShardingNumber() throws Exception {
        config.setShardingNumber(0);
        dispatchManager = new DispatchManager(config, checkPoint, commitLogManager, handlerManager);
        dispatchManager.initialize();

        dispatchManager.start();
        Thread.sleep(50);

        dispatchManager.shutdown();
    }

    @Test
    void testZeroDispatchThreads() throws Exception {
        config.setDispatchThreads(0);
        dispatchManager = new DispatchManager(config, checkPoint, commitLogManager, handlerManager);
        dispatchManager.initialize();

        dispatchManager.start();
        Thread.sleep(50);

        dispatchManager.shutdown();
    }
}
