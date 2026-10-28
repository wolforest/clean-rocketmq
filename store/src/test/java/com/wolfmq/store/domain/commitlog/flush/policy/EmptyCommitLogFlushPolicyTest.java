package com.wolfmq.store.domain.commitlog.flush.policy;

import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.core.mmap.InsertResult;
import com.wolfmq.domain.core.mmap.MappedFileQueue;
import com.wolfmq.domain.domain.mq.dto.EnqueueFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmptyCommitLogFlushPolicyTest {

    private EmptyCommitLogFlushPolicy policy;

    @BeforeEach
    void setUp() {
        MappedFileQueue mappedFileQueue = mock(MappedFileQueue.class);
        policy = new EmptyCommitLogFlushPolicy(mappedFileQueue);
    }

    @Test
    void testFlushReturnsSuccess() {
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
