package com.wolfmq.store.domain.dispatcher;

import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.domain.server.store.CheckPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.wolfmq.store.server.bootstrap.StoreCheckpoint;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommitListenerTest {

    @TempDir
    Path tmpDir;

    private CommitLog commitLog;
    private DispatchQueue queue;
    private CheckPoint checkPoint;
    private final int shardId = 5;
    private CommitConfig commitConfig;

    @BeforeEach
    void setUp() {
        commitConfig = new CommitConfig();
        queue = new DispatchQueue(commitConfig);

        commitLog = mock(CommitLog.class);
        when(commitLog.getShardId()).thenReturn(shardId);
        when(commitLog.getMinOffset()).thenReturn(0L);
        when(commitLog.getMaxOffset()).thenReturn(0L);

        String checkpointPath = tmpDir.resolve("checkpoint").toString();
        checkPoint = new StoreCheckpoint(checkpointPath);
    }

    @Test
    void testGetServiceName() {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint, commitConfig);

        String serviceName = listener.getServiceName();
        assertEquals("CommitListener-" + shardId, serviceName);
    }

    @Test
    void testGetDispatchedOffset_Initial() {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint, commitConfig);

        assertEquals(0, listener.getDispatchedOffset());
    }

    @Test
    void testSetDispatchedOffset() {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint, commitConfig);

        listener.setDispatchedOffset(1000);

        assertEquals(1000, listener.getDispatchedOffset());
    }

    @Test
    void testIncreaseDispatchedOffset() {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint, commitConfig);

        listener.setDispatchedOffset(0);
        long newOffset = listener.increaseDispatchedOffset(500);

        assertEquals(500, newOffset);
        assertEquals(500, listener.getDispatchedOffset());
    }

    @Test
    void testIncreaseDispatchedOffset_Multiple() {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint, commitConfig);

        listener.setDispatchedOffset(0);
        listener.increaseDispatchedOffset(100);
        long finalOffset = listener.increaseDispatchedOffset(50);

        assertEquals(150, finalOffset);
        assertEquals(150, listener.getDispatchedOffset());
    }

    @Test
    void testLifecycle() throws Exception {
        CommitListener listener = new CommitListener(queue, commitLog, checkPoint, commitConfig);

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
            CommitListener listener = new CommitListener(queue, commitLog, checkPoint, commitConfig);
            assertEquals("CommitListener-" + id, listener.getServiceName());
        }
    }
}
