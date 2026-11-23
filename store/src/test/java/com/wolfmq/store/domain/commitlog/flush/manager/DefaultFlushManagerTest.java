package com.wolfmq.store.domain.commitlog.flush.manager;

import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.commitlog.flush.FlushType;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueFuture;
import com.wolfmq.common.core.mmap.InsertResult;
import com.wolfmq.common.core.mmap.MappedFileQueue;
import com.wolfmq.common.domain.offset.QueueProgress;
import com.wolfmq.store.domain.commitlog.file.flush.manager.FlushManager;
import com.wolfmq.store.domain.commitlog.file.flush.manager.DefaultFlushManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DefaultFlushManagerTest {

    private CommitConfig commitConfig;
    private MappedFileQueue mappedFileQueue;
    private QueueProgress queueProgress;

    @BeforeEach
    void setUp() {
        commitConfig = new CommitConfig();
        mappedFileQueue = mock(MappedFileQueue.class);
        queueProgress = mock(QueueProgress.class);
    }

    @Test
    void testConstructorWithShardId() {
        DefaultFlushManager manager = new DefaultFlushManager(commitConfig, mappedFileQueue, queueProgress);
        assertNotNull(manager);
    }

    @Test
    void testConstructorWithoutShardId() {
        DefaultFlushManager manager = new DefaultFlushManager(commitConfig, mappedFileQueue, queueProgress);
        assertNotNull(manager);
    }

    @Test
    void testAsyncFlushReturnsSuccess() {
        commitConfig.setFlushType(FlushType.ASYNC);
        DefaultFlushManager manager = new DefaultFlushManager(commitConfig, mappedFileQueue, queueProgress);

        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(100L);
        when(insertResult.getWroteBytes()).thenReturn(50);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isWaitStore()).thenReturn(false);

        EnqueueFuture future = manager.flush(insertResult, messageBO);
        assertNotNull(future);
    }

    @Test
    void testSyncFlushWithoutWait() {
        commitConfig.setFlushType(FlushType.SYNC);
        DefaultFlushManager manager = new DefaultFlushManager(commitConfig, mappedFileQueue, queueProgress);

        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(100L);
        when(insertResult.getWroteBytes()).thenReturn(50);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isWaitStore()).thenReturn(false);

        EnqueueFuture future = manager.flush(insertResult, messageBO);
        assertNotNull(future);
    }

    @Test
    void testSyncFlushWithWait() {
        commitConfig.setFlushType(FlushType.SYNC);
        commitConfig.setFlushTimeout(5000);
        DefaultFlushManager manager = new DefaultFlushManager(commitConfig, mappedFileQueue, queueProgress);

        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(100L);
        when(insertResult.getWroteBytes()).thenReturn(50);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isWaitStore()).thenReturn(true);

        EnqueueFuture future = manager.flush(insertResult, messageBO);
        assertNotNull(future);
    }

    @Test
    void testAsyncFlushWithWriteCache() {
        commitConfig.setFlushType(FlushType.ASYNC);
        commitConfig.setEnableWriteCache(true);
        DefaultFlushManager manager = new DefaultFlushManager(commitConfig, mappedFileQueue, queueProgress);

        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(100L);
        when(insertResult.getWroteBytes()).thenReturn(50);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isWaitStore()).thenReturn(false);

        EnqueueFuture future = manager.flush(insertResult, messageBO);
        assertNotNull(future);
    }

    @Test
    void testFlushWithZeroWroteBytes() {
        commitConfig.setFlushType(FlushType.ASYNC);
        DefaultFlushManager manager = new DefaultFlushManager(commitConfig, mappedFileQueue, queueProgress);

        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(0L);
        when(insertResult.getWroteBytes()).thenReturn(0);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isWaitStore()).thenReturn(false);

        EnqueueFuture future = manager.flush(insertResult, messageBO);
        assertNotNull(future);
    }
}
