package cn.coderule.wolfmq.broker.domain.transaction;

import cn.coderule.wolfmq.broker.domain.transaction.receipt.Receipt;
import cn.coderule.wolfmq.broker.domain.transaction.receipt.ReceiptRegistry;
import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Receipt lifecycle within Transaction module.
 * Uses real ReceiptRegistry and Receipt - no mocks needed.
 */
public class TransactionReceiptIntegrationTest {

    private TransactionConfig config;
    private ReceiptRegistry registry;

    @BeforeEach
    void setUp() {
        config = new TransactionConfig();
        config.setMaxReceiptNum(100);
        config.setReceiptExpireTime(30000L);
        registry = new ReceiptRegistry(config);
    }

    // ========== Register and Poll ==========

    @Test
    void registerAndPoll_shouldReturnMatchingReceipt() {
        long now = System.currentTimeMillis();
        Receipt receipt = buildReceipt("group1", "tx1", "store1", 100L, 200L, now, 30000L);

        registry.register(receipt);
        Receipt polled = registry.poll("group1", "tx1");

        assertNotNull(polled);
        assertEquals("store1", polled.getStoreGroup());
        assertEquals(100L, polled.getQueueOffset());
        assertEquals(200L, polled.getCommitOffset());
    }

    @Test
    void pollTwice_shouldReturnNullAfterFirstPoll() {
        long now = System.currentTimeMillis();
        Receipt receipt = buildReceipt("group1", "tx1", "store1", 100L, 200L, now, 30000L);

        registry.register(receipt);
        Receipt first = registry.poll("group1", "tx1");
        assertNotNull(first);

        Receipt second = registry.poll("group1", "tx1");
        assertNull(second);
    }

    @Test
    void pollNonExistentKey_shouldReturnNull() {
        Receipt result = registry.poll("nonexistent", "tx1");
        assertNull(result);
    }

    // ========== Expiration ==========

    @Test
    void pollExpiredReceipt_shouldReturnNull() {
        long past = System.currentTimeMillis() - 60000;
        Receipt receipt = buildReceipt("group1", "tx1", "store1", 100L, 200L, past, 1000L);

        registry.register(receipt);
        Receipt polled = registry.poll("group1", "tx1");
        assertNull(polled);
    }

    @Test
    void cleanExpiredReceipts_shouldRemoveExpiredOnes() {
        long now = System.currentTimeMillis();

        Receipt expiredReceipt = buildReceipt("group2", "tx-expired", "store1", 50L, 100L, now - 60000, 1000L);
        Receipt validReceipt = buildReceipt("group2", "tx-valid", "store1", 200L, 300L, now, 30000L);

        registry.register(expiredReceipt);
        registry.register(validReceipt);

        registry.cleanExpiredReceipts();

        Receipt polled = registry.poll("group2", "tx-valid");
        assertNotNull(polled);
        assertEquals(200L, polled.getQueueOffset());

        Receipt expired = registry.poll("group2", "tx-expired");
        assertNull(expired);
    }

    // ========== Capacity ==========

    @Test
    void registerOverCapacity_shouldEvictOldestReceipt() {
        TransactionConfig limitedConfig = new TransactionConfig();
        limitedConfig.setMaxReceiptNum(3);
        ReceiptRegistry limitedRegistry = new ReceiptRegistry(limitedConfig);

        long now = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            Receipt receipt = buildReceipt("group1", "tx1", "store1", i * 10L, i * 20L, now + i * 1000, 30000L);
            limitedRegistry.register(receipt);
        }

        Receipt polled = limitedRegistry.poll("group1", "tx1");
        assertNotNull(polled);
        assertTrue(polled.getQueueOffset() >= 20L);
    }

    // ========== Multi-Key Isolation ==========

    @Test
    void registerDifferentGroups_shouldIsolateReceipts() {
        long now = System.currentTimeMillis();
        Receipt receiptA = buildReceipt("groupA", "tx1", "store1", 100L, 200L, now, 30000L);
        Receipt receiptB = buildReceipt("groupB", "tx1", "store2", 300L, 400L, now, 30000L);

        registry.register(receiptA);
        registry.register(receiptB);

        Receipt polledA = registry.poll("groupA", "tx1");
        assertNotNull(polledA);
        assertEquals("store1", polledA.getStoreGroup());
        assertEquals(100L, polledA.getQueueOffset());

        Receipt polledB = registry.poll("groupB", "tx1");
        assertNotNull(polledB);
        assertEquals("store2", polledB.getStoreGroup());
        assertEquals(300L, polledB.getQueueOffset());
    }

    @Test
    void registerDifferentTransactions_shouldIsolateReceipts() {
        long now = System.currentTimeMillis();
        Receipt receipt1 = buildReceipt("group1", "tx-alpha", "store1", 10L, 20L, now, 30000L);
        Receipt receipt2 = buildReceipt("group1", "tx-beta", "store1", 30L, 40L, now, 30000L);

        registry.register(receipt1);
        registry.register(receipt2);

        Receipt polled1 = registry.poll("group1", "tx-alpha");
        assertNotNull(polled1);
        assertEquals(10L, polled1.getQueueOffset());

        Receipt polled2 = registry.poll("group1", "tx-beta");
        assertNotNull(polled2);
        assertEquals(30L, polled2.getQueueOffset());
    }

    @Test
    void crossPoll_shouldReturnNull() {
        long now = System.currentTimeMillis();
        Receipt receipt = buildReceipt("groupA", "tx1", "store1", 100L, 200L, now, 30000L);

        registry.register(receipt);

        Receipt cross = registry.poll("groupB", "tx1");
        assertNull(cross);

        Receipt cross2 = registry.poll("groupA", "tx-other");
        assertNull(cross2);
    }

    // ========== Remove ==========

    @Test
    void removeRegisteredReceipt_shouldSucceed() {
        long now = System.currentTimeMillis();
        Receipt receipt = buildReceipt("group1", "tx1", "store1", 100L, 200L, now, 30000L);

        registry.register(receipt);
        assertDoesNotThrow(() -> registry.remove(receipt));

        Receipt polled = registry.poll("group1", "tx1");
        assertNull(polled);
    }

    // ========== getMaxExpireTime ==========

    @Test
    void getMaxExpireTime_afterRegistration_shouldReturnMaxExpireTime() {
        long now = System.currentTimeMillis();
        Receipt receipt = buildReceipt("group1", "tx1", "store1", 100L, 200L, now, 30000L);

        registry.register(receipt);
        long maxExpireTime = registry.getMaxExpireTime();
        assertTrue(maxExpireTime >= now);
    }

    // ========== Receipt Comparable ==========

    @Test
    void receiptCompareTo_shouldOrderByExpireTime() {
        long now = System.currentTimeMillis();
        Receipt earlier = buildReceipt("group1", "tx1", "store1", 100L, 200L, now, 10000L);
        Receipt later = buildReceipt("group1", "tx1", "store1", 300L, 400L, now, 30000L);

        assertTrue(earlier.compareTo(later) < 0);
        assertTrue(later.compareTo(earlier) > 0);
        assertEquals(0, earlier.compareTo(earlier));
    }

    // ========== Transaction Roundtrip: Register → Poll → Use ==========

    @Test
    void transactionRoundtrip_registerPollAndVerifyReceiptData() {
        long now = System.currentTimeMillis();

        Receipt receipt = Receipt.builder()
            .storeGroup("store-group-1")
            .topic("order-topic")
            .producerGroup("order-producer")
            .transactionId("order-txn-001")
            .messageId("msg-12345")
            .queueOffset(500L)
            .commitOffset(1000L)
            .checkTimestamp(now)
            .expireMs(30000L)
            .build();

        registry.register(receipt);

        Receipt polled = registry.poll("order-producer", "order-txn-001");
        assertNotNull(polled);
        assertEquals("store-group-1", polled.getStoreGroup());
        assertEquals("msg-12345", polled.getMessageId());
        assertEquals(500L, polled.getQueueOffset());
        assertEquals(1000L, polled.getCommitOffset());
        assertEquals("order-producer", polled.getProducerGroup());
        assertEquals("order-txn-001", polled.getTransactionId());
    }

    @Test
    void transactionRoundtrip_multipleReceiptsForSameKey_shouldReturnLatest() {
        long now = System.currentTimeMillis();

        Receipt older = Receipt.builder()
            .producerGroup("group1")
            .transactionId("tx1")
            .storeGroup("store1")
            .messageId("msg-old")
            .queueOffset(100L)
            .commitOffset(200L)
            .checkTimestamp(now)
            .expireMs(30000L)
            .build();

        Receipt newer = Receipt.builder()
            .producerGroup("group1")
            .transactionId("tx1")
            .storeGroup("store1")
            .messageId("msg-new")
            .queueOffset(300L)
            .commitOffset(400L)
            .checkTimestamp(now + 1000)
            .expireMs(30000L)
            .build();

        registry.register(older);
        registry.register(newer);

        Receipt polled = registry.poll("group1", "tx1");
        assertNotNull(polled);
        assertEquals("msg-new", polled.getMessageId());
    }

    private Receipt buildReceipt(String producerGroup, String transactionId,
                                  String storeGroup, long queueOffset, long commitOffset,
                                  long checkTimestamp, long expireMs) {
        return Receipt.builder()
            .producerGroup(producerGroup)
            .transactionId(transactionId)
            .storeGroup(storeGroup)
            .queueOffset(queueOffset)
            .commitOffset(commitOffset)
            .checkTimestamp(checkTimestamp)
            .expireMs(expireMs)
            .build();
    }
}