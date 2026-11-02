package com.wolfmq.store.server.ha.server.processor;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.mq.dto.EnqueueFuture;
import com.wolfmq.common.domain.mq.dto.EnqueueResult;
import com.wolfmq.common.core.mmap.InsertResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommitLogSynchronizerTest {

    private StoreConfig storeConfig;
    private CommitLogSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        storeConfig = new StoreConfig();
        synchronizer = new CommitLogSynchronizer(storeConfig);
    }

    @Test
    void testGetServiceName() {
        assertEquals("CommitLogSynchronizer", synchronizer.getServiceName());
    }

    @Test
    void testSyncReturnsFutureWhenHADisabled() {
        storeConfig.setEnableHA(false);
        EnqueueFuture enqueueFuture = createEnqueueFuture();

        CompletableFuture<EnqueueResult> result = synchronizer.sync(enqueueFuture);
        assertNotNull(result);
        assertTrue(result.isDone());
    }

    @Test
    void testSyncAddsEventWhenHAEnabled() {
        storeConfig.setEnableHA(true);
        storeConfig.setSlaveTimeout(5000);
        EnqueueFuture enqueueFuture = createEnqueueFuture();

        CompletableFuture<EnqueueResult> result = synchronizer.sync(enqueueFuture);
        assertNull(result);
    }

    @Test
    void testSyncWithNullFutureWhenHADisabled() {
        storeConfig.setEnableHA(false);
        EnqueueFuture enqueueFuture = createEnqueueFuture();
        CompletableFuture<EnqueueResult> result = synchronizer.sync(enqueueFuture);
        assertNotNull(result);
    }

    @Test
    void testSyncWithNullFutureWhenHAEnabled() {
        storeConfig.setEnableHA(true);
        storeConfig.setSlaveTimeout(5000);
        EnqueueFuture enqueueFuture = createEnqueueFuture();
        CompletableFuture<EnqueueResult> result = synchronizer.sync(enqueueFuture);
        assertNull(result);
    }

    private EnqueueFuture createEnqueueFuture() {
        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(100L);
        when(insertResult.getWroteBytes()).thenReturn(50);
        MessageBO messageBO = mock(MessageBO.class);
        return EnqueueFuture.success(insertResult, messageBO);
    }
}
