package cn.coderule.wolfmq.domain.domain.store.domain.mq;

import cn.coderule.wolfmq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.infra.InsertResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnqueueResultTest {

    @Test
    void testConstructor_withStatus() {
        EnqueueResult result = new EnqueueResult(EnqueueStatus.PUT_OK);
        assertEquals(EnqueueStatus.PUT_OK, result.getStatus());
    }

    @Test
    void testConstructor_withStatusAndInsertResult() {
        InsertResult insertResult = mock(InsertResult.class);
        EnqueueResult result = new EnqueueResult(EnqueueStatus.PUT_OK, insertResult);

        assertEquals(EnqueueStatus.PUT_OK, result.getStatus());
        assertEquals(insertResult, result.getInsertResult());
    }

    @Test
    void testIsSuccess_whenPutOk_returnsTrue() {
        EnqueueResult result = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .build();

        assertTrue(result.isSuccess());
    }

    @Test
    void testIsFailure_whenPutOk_returnsFalse() {
        EnqueueResult result = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .build();

        assertFalse(result.isFailure());
    }

    @Test
    void testIsFailure_whenServiceNotAvailable_returnsTrue() {
        EnqueueResult result = EnqueueResult.builder()
            .status(EnqueueStatus.SERVICE_NOT_AVAILABLE)
            .build();

        assertTrue(result.isFailure());
    }

    @Test
    void testIsFailure_whenFlushTimeout_returnsFalse() {
        EnqueueResult result = EnqueueResult.builder()
            .status(EnqueueStatus.FLUSH_DISK_TIMEOUT)
            .build();

        assertFalse(result.isFailure());
    }

    @Test
    void testSuccess_createsResultWithMessageDetails() {
        InsertResult insertResult = mock(InsertResult.class);
        MessageBO messageBO = mock(MessageBO.class);

        when(messageBO.getStoreGroup()).thenReturn("storeGroup1");
        when(messageBO.getUniqueKey()).thenReturn("messageId123");
        when(messageBO.getTransactionId()).thenReturn("tx123");
        when(messageBO.getCommitOffset()).thenReturn(100L);
        when(messageBO.getQueueOffset()).thenReturn(50L);
        when(messageBO.getQueueId()).thenReturn(1);

        EnqueueResult result = EnqueueResult.success(insertResult, messageBO);

        assertTrue(result.isSuccess());
        assertEquals("storeGroup1", result.getStoreGroup());
        assertEquals("messageId123", result.getMessageId());
        assertEquals("tx123", result.getTransactionId());
        assertEquals(100L, result.getCommitOffset());
        assertEquals(50L, result.getQueueOffset());
        assertEquals(1, result.getQueueId());
    }

    @Test
    void testFailure_createsResultWithErrorStatus() {
        EnqueueResult result = EnqueueResult.failure(EnqueueStatus.SERVICE_NOT_AVAILABLE);

        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals(EnqueueStatus.SERVICE_NOT_AVAILABLE, result.getStatus());
    }
}