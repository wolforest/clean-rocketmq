package com.wolfmq.store.domain.commitlog.flush.manager;

import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.core.mmap.MappedFileQueue;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueFuture;
import com.wolfmq.store.domain.commitlog.file.flush.manager.SyncFlushManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SyncFlushManagerTest {

    private MappedFileQueue mappedFileQueue;
    private SyncFlushManager policy;

    @BeforeEach
    void setUp() {
        mappedFileQueue = mock(MappedFileQueue.class);
        policy = new SyncFlushManager(mappedFileQueue);
    }

    @Test
    void testFlushSuccess() {
        when(mappedFileQueue.flush(0)).thenReturn(true);
        InsertResult insertResult = mock(InsertResult.class);
        MessageBO messageBO = mock(MessageBO.class);

        EnqueueFuture result = policy.flush(insertResult, messageBO);

        assertNotNull(result);
    }

    @Test
    void testFlushFailure() {
        when(mappedFileQueue.flush(0)).thenReturn(false);
        InsertResult insertResult = mock(InsertResult.class);
        MessageBO messageBO = mock(MessageBO.class);

        EnqueueFuture result = policy.flush(insertResult, messageBO);

        assertNotNull(result);
    }

    @Test
    void testStartDoesNotThrow() {
        assertDoesNotThrow(() -> policy.start());
    }

    @Test
    void testShutdownDoesNotThrow() {
        assertDoesNotThrow(() -> policy.shutdown());
    }
}
