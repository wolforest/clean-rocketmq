package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.server.CheckPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.test.ConfigMock;
import cn.coderule.minimq.store.server.bootstrap.StoreCheckpoint;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommitListenerTest {

    @TempDir
    Path tmpDir;

    private CommitLog commitLog;
    private DispatchQueue queue;
    private CheckPoint checkPoint;
    private int shardId = 5;

    @BeforeEach
    void setUp() {
        queue = new DispatchQueue(new cn.coderule.minimq.domain.config.store.CommitConfig());

        commitLog = mock(CommitLog.class);
        when(commitLog.getShardId()).thenReturn(shardId);
        when(commitLog.getMinOffset()).thenReturn(0L);
        when(commitLog.getMaxOffset()).thenReturn(0L);

        String checkpointPath = tmpDir.resolve("checkpoint").toString();
        checkPoint = new StoreCheckpoint(checkpointPath);
    }

    @Test
    void testGetServiceName() {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint);

        String serviceName = listener.getServiceName();
        assertEquals("CommitListener-" + shardId, serviceName);
    }

    @Test
    void testGetDispatchedOffset_Initial() {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint);

        assertEquals(0, listener.getDispatchedOffset());
    }

    @Test
    void testSetDispatchedOffset() {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint);

        listener.setDispatchedOffset(1000);

        assertEquals(1000, listener.getDispatchedOffset());
    }

    @Test
    void testIncreaseDispatchedOffset() {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint);

        listener.setDispatchedOffset(0);
        long newOffset = listener.increaseDispatchedOffset(500);

        assertEquals(500, newOffset);
        assertEquals(500, listener.getDispatchedOffset());
    }

    @Test
    void testIncreaseDispatchedOffset_Multiple() {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint);

        listener.setDispatchedOffset(0);
        listener.increaseDispatchedOffset(100);
        long finalOffset = listener.increaseDispatchedOffset(50);

        assertEquals(150, finalOffset);
        assertEquals(150, listener.getDispatchedOffset());
    }

    @Test
    void testLifecycle() throws Exception {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint);

        listener.start();
        Thread.sleep(50);

        assertFalse(listener.isStopped());

        listener.shutdown();
        Thread.sleep(50);

        assertTrue(listener.isStopped());
    }

    @Test
    void testServiceNameFormat() {
        for (int id = 0; id < 10; id++) {
            when(commitLog.getShardId()).thenReturn(id);
            CommitListener listener = new CommitListener(queue, commitLog, checkPoint);
            assertEquals("CommitListener-" + id, listener.getServiceName());
        }
    }
}
