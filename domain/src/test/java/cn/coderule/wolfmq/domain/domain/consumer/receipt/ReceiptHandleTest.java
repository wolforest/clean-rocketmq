package cn.coderule.wolfmq.domain.domain.consumer.receipt;

import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptHandleTest {

    @Test
    void encode_then_decode_roundtrip() {
        ReceiptHandle handle = ReceiptHandle.builder()
            .startOffset(100L)
            .retrieveTime(2000L)
            .invisibleTime(30000L)
            .reviveQueueId(1)
            .topicType("0")
            .brokerName("brokerA")
            .queueId(3)
            .offset(500L)
            .commitLogOffset(600L)
            .receiptHandle("original")
            .build();

        String encoded = handle.encode();
        ReceiptHandle decoded = ReceiptHandle.decode(encoded);

        assertEquals(100L, decoded.getStartOffset());
        assertEquals(2000L, decoded.getRetrieveTime());
        assertEquals(30000L, decoded.getInvisibleTime());
        assertEquals(1, decoded.getReviveQueueId());
        assertEquals("0", decoded.getTopicType());
        assertEquals("brokerA", decoded.getBrokerName());
        assertEquals(3, decoded.getQueueId());
        assertEquals(500L, decoded.getOffset());
        assertEquals(600L, decoded.getCommitLogOffset());
    }

    @Test
    void decode_withoutCommitLogOffset() {
        String str = "100" + MessageConst.KEY_SEPARATOR + "2000" + MessageConst.KEY_SEPARATOR + "30000"
            + MessageConst.KEY_SEPARATOR + "1" + MessageConst.KEY_SEPARATOR + "0"
            + MessageConst.KEY_SEPARATOR + "brokerA" + MessageConst.KEY_SEPARATOR + "3"
            + MessageConst.KEY_SEPARATOR + "500";
        ReceiptHandle decoded = ReceiptHandle.decode(str);
        assertEquals(100L, decoded.getStartOffset());
        assertEquals(-1L, decoded.getCommitLogOffset());
    }

    @Test
    void decode_tooFewElements_throws() {
        String str = "100" + MessageConst.KEY_SEPARATOR + "2000";
        assertThrows(IllegalArgumentException.class, () -> ReceiptHandle.decode(str));
    }

    @Test
    void isRetryTopic_normal() {
        ReceiptHandle handle = ReceiptHandle.builder()
            .startOffset(0L).retrieveTime(0L).invisibleTime(0L)
            .reviveQueueId(0).topicType("0").brokerName("b").queueId(0).offset(0L).receiptHandle("rh")
            .build();
        assertFalse(handle.isRetryTopic());
    }

    @Test
    void isRetryTopic_v1() {
        ReceiptHandle handle = ReceiptHandle.builder()
            .startOffset(0L).retrieveTime(0L).invisibleTime(0L)
            .reviveQueueId(0).topicType("1").brokerName("b").queueId(0).offset(0L).receiptHandle("rh")
            .build();
        assertTrue(handle.isRetryTopic());
    }

    @Test
    void isRetryTopic_v2() {
        ReceiptHandle handle = ReceiptHandle.builder()
            .startOffset(0L).retrieveTime(0L).invisibleTime(0L)
            .reviveQueueId(0).topicType("2").brokerName("b").queueId(0).offset(0L).receiptHandle("rh")
            .build();
        assertTrue(handle.isRetryTopic());
    }

    @Test
    void getRealTopic_normalTopic() {
        ReceiptHandle handle = ReceiptHandle.builder()
            .startOffset(0L).retrieveTime(0L).invisibleTime(0L)
            .reviveQueueId(0).topicType("0").brokerName("b").queueId(0).offset(0L).receiptHandle("rh")
            .build();
        assertEquals("myTopic", handle.getRealTopic("myTopic", "myGroup"));
    }

    @Test
    void getRealTopic_retryV1() {
        ReceiptHandle handle = ReceiptHandle.builder()
            .startOffset(0L).retrieveTime(0L).invisibleTime(0L)
            .reviveQueueId(0).topicType("1").brokerName("b").queueId(0).offset(0L).receiptHandle("rh")
            .build();
        String result = handle.getRealTopic("myTopic", "myGroup");
        assertTrue(result.startsWith("%RETRY%"));
        assertTrue(result.contains("myGroup"));
        assertTrue(result.contains("myTopic"));
    }

    @Test
    void getRealTopic_retryV2() {
        ReceiptHandle handle = ReceiptHandle.builder()
            .startOffset(0L).retrieveTime(0L).invisibleTime(0L)
            .reviveQueueId(0).topicType("2").brokerName("b").queueId(0).offset(0L).receiptHandle("rh")
            .build();
        String result = handle.getRealTopic("myTopic", "myGroup");
        assertTrue(result.startsWith("%RETRY%"));
        assertTrue(result.contains("+myTopic"));
    }

    @Test
    void isExpired_pastTime() {
        ReceiptHandle handle = ReceiptHandle.builder()
            .startOffset(0L).retrieveTime(0L).invisibleTime(0L)
            .reviveQueueId(0).topicType("0").brokerName("b").queueId(0).offset(0L).receiptHandle("rh")
            .build();
        // nextVisibleTime = retrieveTime + invisibleTime = 0, which is <= now
        assertTrue(handle.isExpired());
    }

    @Test
    void builder_setsNextVisibleTime() {
        ReceiptHandle handle = ReceiptHandle.builder()
            .startOffset(100L).retrieveTime(5000L).invisibleTime(30000L)
            .reviveQueueId(1).topicType("0").brokerName("b").queueId(3).offset(500L).receiptHandle("rh")
            .build();
        assertEquals(35000L, handle.getNextVisibleTime());
    }
}