package cn.coderule.wolfmq.rpc.common.core.relay.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionResultTest {

    @Test
    void testConstructor() {
        String brokerName = "broker-a";
        String topic = "test-topic";
        long tranStateTableOffset = 100L;
        long commitLogOffset = 200L;
        String transactionId = "txn-123";
        long checkTimestamp = System.currentTimeMillis();
        long expireMs = 60000L;
        
        TransactionResult result = new TransactionResult(brokerName, topic, tranStateTableOffset, commitLogOffset, transactionId, checkTimestamp, expireMs);
        
        assertNotNull(result);
        assertEquals(brokerName, result.getBrokerName());
        assertEquals(topic, result.getTopic());
        assertEquals(tranStateTableOffset, result.getTranStateTableOffset());
        assertEquals(commitLogOffset, result.getCommitLogOffset());
        assertEquals(transactionId, result.getTransactionId());
        assertEquals(checkTimestamp, result.getCheckTimestamp());
        assertEquals(expireMs, result.getExpireMs());
        assertEquals(checkTimestamp + expireMs, result.getExpireTime());
    }

    @Test
    void testEqualsAndHashCode() {
        long now = System.currentTimeMillis();
        TransactionResult result1 = new TransactionResult("broker-a", "topic", 100L, 200L, "txn-123", now, 60000L);
        TransactionResult result2 = new TransactionResult("broker-a", "topic", 100L, 200L, "txn-123", now, 60000L);
        TransactionResult result3 = new TransactionResult("broker-b", "topic", 100L, 200L, "txn-123", now, 60000L);
        
        assertEquals(result1, result2);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1, result3);
    }

    @Test
    void testCompareTo() {
        long now = System.currentTimeMillis();
        TransactionResult result1 = new TransactionResult("broker-a", "topic", 100L, 200L, "txn-1", now, 60000L);
        TransactionResult result2 = new TransactionResult("broker-a", "topic", 100L, 200L, "txn-2", now + 1000, 60000L);
        
        assertTrue(result1.compareTo(result2) < 0);
        assertTrue(result2.compareTo(result1) > 0);
        assertEquals(0, result1.compareTo(result1));
    }

    @Test
    void testToString() {
        long now = System.currentTimeMillis();
        TransactionResult result = new TransactionResult("broker-a", "topic", 100L, 200L, "txn-123", now, 60000L);
        
        String str = result.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("broker-a"));
        assertTrue(str.contains("txn-123"));
    }
}
