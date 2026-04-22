package cn.coderule.wolfmq.domain.domain.consumer.ack.broker;

import cn.coderule.wolfmq.domain.core.enums.consume.AckStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AckResultTest {

    @Test
    void testDefaultConstructor() {
        AckResult result = new AckResult();
        assertNotNull(result);
    }

    @Test
    void testSuccess() {
        AckResult result = AckResult.success();
        assertNotNull(result);
        assertEquals(AckStatus.OK, result.getStatus());
        assertTrue(result.isSuccess());
    }

    @Test
    void testFailure() {
        AckResult result = AckResult.failure();
        assertNotNull(result);
        assertEquals(AckStatus.NO_EXIST, result.getStatus());
        assertFalse(result.isSuccess());
    }

    @Test
    void testAppendCheckpointFailure() {
        AckResult result = AckResult.success();
        result.appendCheckpointFailure();
        
        assertEquals(AckStatus.NO_EXIST, result.getStatus());
        assertFalse(result.isSuccess());
    }

    @Test
    void testBuilder() {
        AckResult result = AckResult.builder()
            .status(AckStatus.OK)
            .extraInfo("extra info")
            .receiptStr("receipt-123")
            .popTime(System.currentTimeMillis())
            .invisibleTime(30000)
            .reviveQueueId(1)
            .commitOffset(100)
            .build();
        
        assertNotNull(result);
        assertEquals(AckStatus.OK, result.getStatus());
        assertEquals("extra info", result.getExtraInfo());
        assertEquals("receipt-123", result.getReceiptStr());
        assertTrue(result.isSuccess());
    }

    @Test
    void testSettersAndGetters() {
        AckResult result = new AckResult();
        
        result.setStatus(AckStatus.OK);
        assertEquals(AckStatus.OK, result.getStatus());
        
        result.setExtraInfo("test extra");
        assertEquals("test extra", result.getExtraInfo());
        
        result.setReceiptStr("test-receipt");
        assertEquals("test-receipt", result.getReceiptStr());
        
        result.setPopTime(12345678L);
        assertEquals(12345678L, result.getPopTime());
        
        result.setInvisibleTime(60000L);
        assertEquals(60000L, result.getInvisibleTime());
        
        result.setReviveQueueId(2);
        assertEquals(2, result.getReviveQueueId());
        
        result.setCommitOffset(200);
        assertEquals(200, result.getCommitOffset());
    }

    @Test
    void testToString() {
        AckResult result = AckResult.success();
        String str = result.toString();
        assertNotNull(str);
        assertTrue(str.contains("AckResult"));
    }
}
