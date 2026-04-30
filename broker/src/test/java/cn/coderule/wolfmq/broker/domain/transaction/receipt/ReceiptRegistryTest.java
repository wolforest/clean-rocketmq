package cn.coderule.wolfmq.broker.domain.transaction.receipt;

import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptRegistryTest {

    private ReceiptRegistry registry;

    @BeforeEach
    void setUp() {
        TransactionConfig config = new TransactionConfig();
        config.setMaxReceiptNum(100);
        registry = new ReceiptRegistry(config);
    }

    @Test
    void testRegisterReceipt() {
        Receipt receipt = createReceipt("group1", "tx1", "broker1", 100L, 200L, System.currentTimeMillis(), 30000L);
        assertDoesNotThrow(() -> registry.register(receipt));
    }

    @Test
    void testPollReceipt() {
        long now = System.currentTimeMillis();
        Receipt receipt = createReceipt("group1", "tx1", "broker1", 100L, 200L, now, 30000L);

        registry.register(receipt);
        Receipt polled = registry.poll("group1", "tx1");
        assertNotNull(polled);
        assertEquals(100L, polled.getQueueOffset());
    }

    @Test
    void testPollExpiredReceipt() {
        long now = System.currentTimeMillis();
        Receipt receipt = createReceipt("group1", "tx1", "broker1", 100L, 200L, now - 60000, 1000L);

        registry.register(receipt);
        Receipt polled = registry.poll("group1", "tx1");
        assertNull(polled);
    }

    @Test
    void testRemoveReceipt() {
        long now = System.currentTimeMillis();
        Receipt receipt = createReceipt("group1", "tx1", "broker1", 100L, 200L, now, 30000L);

        registry.register(receipt);
        assertDoesNotThrow(() -> registry.remove(receipt));
    }

    @Test
    void testRegisterMaxReceiptNum() {
        TransactionConfig config = new TransactionConfig();
        config.setMaxReceiptNum(3);
        ReceiptRegistry limitedRegistry = new ReceiptRegistry(config);

        long now = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            Receipt receipt = createReceipt("group1", "tx1", "broker1", i, i, now + i * 1000, 30000L);
            limitedRegistry.register(receipt);
        }
    }

    @Test
    void testPollNonExistentKey() {
        Receipt result = registry.poll("nonexistent", "tx1");
        assertNull(result);
    }

    @Test
    void testCleanExpiredReceipts() {
        long now = System.currentTimeMillis();
        Receipt receipt = createReceipt("group1", "tx1", "broker1", 100L, 200L, now - 60000, 1000L);

        registry.register(receipt);
        registry.cleanExpiredReceipts();
    }

    private Receipt createReceipt(String producerGroup, String transactionId,
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