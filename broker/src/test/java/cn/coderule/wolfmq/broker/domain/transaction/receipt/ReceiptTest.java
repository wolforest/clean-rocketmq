package cn.coderule.wolfmq.broker.domain.transaction.receipt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptTest {

    @Test
    void testBuildKey() {
        String key = Receipt.buildKey("myGroup", "tx123");
        assertEquals("myGroup@tx123", key);
    }

    @Test
    void testGetKey() {
        Receipt receipt = Receipt.builder()
            .producerGroup("myGroup")
            .transactionId("tx123")
            .build();

        assertEquals("myGroup@tx123", receipt.getKey());
    }

    @Test
    void testGetExpireTime() {
        Receipt receipt = Receipt.builder()
            .checkTimestamp(1000L)
            .expireMs(5000L)
            .build();

        assertEquals(6000L, receipt.getExpireTime());
    }

    @Test
    void testEquals_SameValues() {
        Receipt r1 = Receipt.builder()
            .storeGroup("broker1")
            .transactionId("tx1")
            .queueOffset(100L)
            .commitOffset(200L)
            .checkTimestamp(1000L)
            .expireMs(5000L)
            .build();

        Receipt r2 = Receipt.builder()
            .storeGroup("broker1")
            .transactionId("tx1")
            .queueOffset(100L)
            .commitOffset(200L)
            .checkTimestamp(1000L)
            .expireMs(5000L)
            .build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testEquals_DifferentValues() {
        Receipt r1 = Receipt.builder()
            .storeGroup("broker1")
            .transactionId("tx1")
            .queueOffset(100L)
            .commitOffset(200L)
            .checkTimestamp(1000L)
            .expireMs(5000L)
            .build();

        Receipt r2 = Receipt.builder()
            .storeGroup("broker2")
            .transactionId("tx1")
            .queueOffset(100L)
            .commitOffset(200L)
            .checkTimestamp(1000L)
            .expireMs(5000L)
            .build();

        assertNotEquals(r1, r2);
    }

    @Test
    void testCompareTo_OrderByExpireTime() {
        Receipt r1 = Receipt.builder()
            .storeGroup("a")
            .transactionId("tx1")
            .queueOffset(10L)
            .commitOffset(20L)
            .checkTimestamp(1000L)
            .expireMs(5000L)
            .build();

        Receipt r2 = Receipt.builder()
            .storeGroup("a")
            .transactionId("tx1")
            .queueOffset(10L)
            .commitOffset(20L)
            .checkTimestamp(2000L)
            .expireMs(5000L)
            .build();

        assertTrue(r1.compareTo(r2) < 0);
        assertTrue(r2.compareTo(r1) > 0);
        assertEquals(0, r1.compareTo(r1));
    }

    @Test
    void testBuilder() {
        Receipt receipt = Receipt.builder()
            .storeGroup("broker1")
            .topic("testTopic")
            .producerGroup("producerGroup1")
            .transactionId("tx1")
            .messageId("msg1")
            .queueOffset(100L)
            .commitOffset(200L)
            .checkTimestamp(1000L)
            .expireMs(5000L)
            .build();

        assertEquals("broker1", receipt.getStoreGroup());
        assertEquals("testTopic", receipt.getTopic());
        assertEquals("producerGroup1", receipt.getProducerGroup());
        assertEquals("tx1", receipt.getTransactionId());
        assertEquals("msg1", receipt.getMessageId());
        assertEquals(100L, receipt.getQueueOffset());
        assertEquals(200L, receipt.getCommitOffset());
    }
}