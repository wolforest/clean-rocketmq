package cn.coderule.wolfmq.store.domain.commitlog.flush.policy;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import cn.coderule.wolfmq.domain.domain.store.infra.MappedFileQueue;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmptyCommitLogFlushPolicyTest {

    private MappedFileQueue mappedFileQueue;
    private EmptyCommitLogFlushPolicy policy;

    @BeforeEach
    void setUp() {
        mappedFileQueue = mock(MappedFileQueue.class);
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