package com.wolfmq.store.domain.dispatcher;

import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.store.domain.commitlog.file.FileCommitLog;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.store.domain.dispatcher.dispatch.CommitListener;
import com.wolfmq.store.domain.dispatcher.dispatch.DispatchQueue;
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

    private FileCommitLog commitLog;
    private DispatchQueue queue;
    private QueueProgress queueProgress;
    private CommitConfig commitConfig;

    @BeforeEach
    void setUp() {
        commitConfig = new CommitConfig();
        queue = new DispatchQueue(commitConfig);

        commitLog = mock(FileCommitLog.class);
        when(commitLog.getMinOffset()).thenReturn(0L);
        when(commitLog.getMaxOffset()).thenReturn(0L);
        when(commitLog.getOffsetCodec()).thenReturn(new OffsetCodec(1));

        queueProgress = mock(QueueProgress.class);
    }

    @Test
    void testGetServiceName() {
        CommitListener listener = new CommitListener(queue, commitLog, queueProgress, commitConfig);

        String serviceName = listener.getServiceName();
        assertEquals("CommitListener-0", serviceName);
    }

    @Test
    void testGetDispatchedOffset_Initial() {
        CommitListener listener = new CommitListener(queue, commitLog, queueProgress, commitConfig);

        assertEquals(0, listener.getDispatchedOffset());
    }

    @Test
    void testSetDispatchedOffset() {
        CommitListener listener = new CommitListener(queue, commitLog, queueProgress, commitConfig);

        listener.setDispatchedOffset(1000);

        assertEquals(1000, listener.getDispatchedOffset());
    }

    @Test
    void testIncreaseDispatchedOffset() {
        CommitListener listener = new CommitListener(queue, commitLog, queueProgress, commitConfig);

        listener.setDispatchedOffset(0);
        long newOffset = listener.increaseDispatchedOffset(500);

        assertEquals(500, newOffset);
        assertEquals(500, listener.getDispatchedOffset());
    }

    @Test
    void testIncreaseDispatchedOffset_Multiple() {
        CommitListener listener = new CommitListener(queue, commitLog, queueProgress, commitConfig);

        listener.setDispatchedOffset(0);
        listener.increaseDispatchedOffset(100);
        long finalOffset = listener.increaseDispatchedOffset(50);

        assertEquals(150, finalOffset);
        assertEquals(150, listener.getDispatchedOffset());
    }

    @Test
    void testLifecycle() throws Exception {
        CommitListener listener = new CommitListener(queue, commitLog, queueProgress, commitConfig);

        listener.start();
        Thread.sleep(50);

        assertFalse(listener.isStopped());

        listener.shutdown();
        Thread.sleep(50);

        assertTrue(listener.isStopped());
    }

    @Test
    void testServiceNameFormat() {
        CommitListener listener = new CommitListener(queue, commitLog, queueProgress, commitConfig);
        assertEquals("CommitListener-0", listener.getServiceName());
    }
}
