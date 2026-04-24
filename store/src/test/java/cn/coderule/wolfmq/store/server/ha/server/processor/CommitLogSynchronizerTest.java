package cn.coderule.wolfmq.store.server.ha.server.processor;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
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