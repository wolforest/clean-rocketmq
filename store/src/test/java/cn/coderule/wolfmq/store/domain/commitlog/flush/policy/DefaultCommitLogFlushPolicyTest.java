package cn.coderule.wolfmq.store.domain.commitlog.flush.policy;

import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.core.enums.store.FlushType;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DefaultCommitLogFlushPolicyTest {

    private CommitConfig commitConfig;
    private MappedFileQueue mappedFileQueue;
    private CheckPoint checkPoint;

    @BeforeEach
    void setUp() {
        commitConfig = new CommitConfig();
        mappedFileQueue = mock(MappedFileQueue.class);
        checkPoint = mock(CheckPoint.class);
    }

    @Test
    void testConstructorWithShardId() {
        DefaultCommitLogFlushPolicy policy = new DefaultCommitLogFlushPolicy(1, commitConfig, mappedFileQueue, checkPoint);
        assertNotNull(policy);
    }

    @Test
    void testConstructorWithoutShardId() {
        DefaultCommitLogFlushPolicy policy = new DefaultCommitLogFlushPolicy(commitConfig, mappedFileQueue, checkPoint);
        assertNotNull(policy);
    }

    @Test
    void testAsyncFlushReturnsSuccess() {
        commitConfig.setFlushType(FlushType.ASYNC);
        DefaultCommitLogFlushPolicy policy = new DefaultCommitLogFlushPolicy(commitConfig, mappedFileQueue, checkPoint);

        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(100L);
        when(insertResult.getWroteBytes()).thenReturn(50);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isWaitStore()).thenReturn(false);

        EnqueueFuture future = policy.flush(insertResult, messageBO);
        assertNotNull(future);
    }

    @Test
    void testSyncFlushWithoutWait() {
        commitConfig.setFlushType(FlushType.SYNC);
        DefaultCommitLogFlushPolicy policy = new DefaultCommitLogFlushPolicy(commitConfig, mappedFileQueue, checkPoint);

        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(100L);
        when(insertResult.getWroteBytes()).thenReturn(50);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isWaitStore()).thenReturn(false);

        EnqueueFuture future = policy.flush(insertResult, messageBO);
        assertNotNull(future);
    }

    @Test
    void testSyncFlushWithWait() {
        commitConfig.setFlushType(FlushType.SYNC);
        commitConfig.setFlushTimeout(5000);
        DefaultCommitLogFlushPolicy policy = new DefaultCommitLogFlushPolicy(commitConfig, mappedFileQueue, checkPoint);

        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(100L);
        when(insertResult.getWroteBytes()).thenReturn(50);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isWaitStore()).thenReturn(true);

        EnqueueFuture future = policy.flush(insertResult, messageBO);
        assertNotNull(future);
    }

    @Test
    void testAsyncFlushWithWriteCache() {
        commitConfig.setFlushType(FlushType.ASYNC);
        commitConfig.setEnableWriteCache(true);
        DefaultCommitLogFlushPolicy policy = new DefaultCommitLogFlushPolicy(commitConfig, mappedFileQueue, checkPoint);

        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(100L);
        when(insertResult.getWroteBytes()).thenReturn(50);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isWaitStore()).thenReturn(false);

        EnqueueFuture future = policy.flush(insertResult, messageBO);
        assertNotNull(future);
    }

    @Test
    void testFlushWithZeroWroteBytes() {
        commitConfig.setFlushType(FlushType.ASYNC);
        DefaultCommitLogFlushPolicy policy = new DefaultCommitLogFlushPolicy(commitConfig, mappedFileQueue, checkPoint);

        InsertResult insertResult = mock(InsertResult.class);
        when(insertResult.getWroteOffset()).thenReturn(0L);
        when(insertResult.getWroteBytes()).thenReturn(0);
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.isWaitStore()).thenReturn(false);

        EnqueueFuture future = policy.flush(insertResult, messageBO);
        assertNotNull(future);
    }
}