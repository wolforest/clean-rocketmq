package com.wolfmq.store.domain.commitlog.flush.policy;

import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.core.mmap.InsertResult;
import com.wolfmq.domain.core.mmap.MappedFileQueue;
import com.wolfmq.domain.domain.mq.dto.EnqueueFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SyncCommitLogFlushPolicyTest {

    private MappedFileQueue mappedFileQueue;
    private SyncCommitLogFlushPolicy policy;

    @BeforeEach
    void setUp() {
        mappedFileQueue = mock(MappedFileQueue.class);
        policy = new SyncCommitLogFlushPolicy(mappedFileQueue);
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
