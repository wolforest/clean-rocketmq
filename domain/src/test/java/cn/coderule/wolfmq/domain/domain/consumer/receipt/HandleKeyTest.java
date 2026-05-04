package cn.coderule.wolfmq.domain.domain.consumer.receipt;

import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandleKeyTest {

    private ReceiptHandle createReceiptHandle(String receipt) {
        return ReceiptHandle.builder()
            .startOffset(100L)
            .retrieveTime(2000L)
            .invisibleTime(30000L)
            .reviveQueueId(1)
            .topicType("0")
            .brokerName("brokerA")
            .queueId(3)
            .offset(500L)
            .commitLogOffset(600L)
            .receiptHandle(receipt)
            .build();
    }

    @Test
    void equals_sameValues() {
        ReceiptHandle rh1 = createReceiptHandle("receipt1");
        ReceiptHandle rh2 = createReceiptHandle("receipt2");
        HandleKey key1 = new HandleKey(rh1);
        HandleKey key2 = new HandleKey(rh2);
        assertEquals(key1, key2);
    }

    @Test
    void equals_differentBroker() {
        ReceiptHandle rh1 = ReceiptHandle.builder()
            .startOffset(100L).retrieveTime(2000L).invisibleTime(30000L)
            .reviveQueueId(1).topicType("0").brokerName("brokerA").queueId(3).offset(500L).receiptHandle("r1")
            .build();
        ReceiptHandle rh2 = ReceiptHandle.builder()
            .startOffset(100L).retrieveTime(2000L).invisibleTime(30000L)
            .reviveQueueId(1).topicType("0").brokerName("brokerB").queueId(3).offset(500L).receiptHandle("r2")
            .build();
        HandleKey key1 = new HandleKey(rh1);
        HandleKey key2 = new HandleKey(rh2);
        assertNotEquals(key1, key2);
    }

    @Test
    void equals_differentOffset() {
        ReceiptHandle rh1 = ReceiptHandle.builder()
            .startOffset(100L).retrieveTime(2000L).invisibleTime(30000L)
            .reviveQueueId(1).topicType("0").brokerName("brokerA").queueId(3).offset(500L).receiptHandle("r1")
            .build();
        ReceiptHandle rh2 = ReceiptHandle.builder()
            .startOffset(100L).retrieveTime(2000L).invisibleTime(30000L)
            .reviveQueueId(1).topicType("0").brokerName("brokerA").queueId(3).offset(999L).receiptHandle("r2")
            .build();
        HandleKey key1 = new HandleKey(rh1);
        HandleKey key2 = new HandleKey(rh2);
        assertNotEquals(key1, key2);
    }

    @Test
    void hashCode_sameValues() {
        ReceiptHandle rh1 = createReceiptHandle("r1");
        ReceiptHandle rh2 = createReceiptHandle("r2");
        HandleKey key1 = new HandleKey(rh1);
        HandleKey key2 = new HandleKey(rh2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    void getOriginalHandle() {
        ReceiptHandle rh = createReceiptHandle("myReceipt");
        HandleKey key = new HandleKey(rh);
        assertEquals("myReceipt", key.getOriginalHandle());
    }

    @Test
    void getBroker() {
        ReceiptHandle rh = createReceiptHandle("myReceipt");
        HandleKey key = new HandleKey(rh);
        assertEquals("brokerA", key.getBroker());
    }

    @Test
    void getQueueId() {
        ReceiptHandle rh = createReceiptHandle("myReceipt");
        HandleKey key = new HandleKey(rh);
        assertEquals(3, key.getQueueId());
    }

    @Test
    void getOffset() {
        ReceiptHandle rh = createReceiptHandle("myReceipt");
        HandleKey key = new HandleKey(rh);
        assertEquals(500L, key.getOffset());
    }
}