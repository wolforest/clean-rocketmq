package cn.coderule.wolfmq.domain.domain.transaction;

import cn.coderule.wolfmq.domain.core.enums.TransactionType;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class SubmitRequestTest {

    @Test
    void testBuilder() {
        RequestContext ctx = RequestContext.builder().build();
        SubmitRequest request = SubmitRequest.builder()
            .requestContext(ctx)
            .transactionId("tx1")
            .messageId("msg1")
            .storeGroup("store1")
            .topicName("topic1")
            .producerGroup("pg1")
            .fromCheck(false)
            .transactionFlag(0)
            .transactionType(TransactionType.COMMIT)
            .queueOffset(100L)
            .commitOffset(200L)
            .build();

        assertEquals(ctx, request.getRequestContext());
        assertEquals("tx1", request.getTransactionId());
        assertEquals("msg1", request.getMessageId());
        assertEquals("store1", request.getStoreGroup());
        assertEquals("topic1", request.getTopicName());
        assertEquals("pg1", request.getProducerGroup());
        assertFalse(request.isFromCheck());
        assertEquals(TransactionType.COMMIT, request.getTransactionType());
        assertEquals(100L, request.getQueueOffset());
        assertEquals(200L, request.getCommitOffset());
    }

    @Test
    void testDefaultTimeout() {
        SubmitRequest request = new SubmitRequest();
        assertEquals(Duration.ofSeconds(2).toMillis(), request.getTimeout());
    }

    @Test
    void testSetters() {
        SubmitRequest request = new SubmitRequest();
        request.setTransactionId("tx2");
        request.setFromCheck(true);
        request.setTimeout(5000L);

        assertEquals("tx2", request.getTransactionId());
        assertTrue(request.isFromCheck());
        assertEquals(5000L, request.getTimeout());
    }
}