package cn.coderule.wolfmq.broker.domain.transaction.service;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BatchCommitContextTest {

    private BatchCommitContext batchCommitContext;

    @BeforeEach
    void setUp() {
        batchCommitContext = new BatchCommitContext();
    }

    @Test
    void testConstructor() {
        assertNotNull(batchCommitContext);
        assertNotNull(batchCommitContext.getSendMap());
        assertFalse(batchCommitContext.isOverflow());
        assertTrue(batchCommitContext.getStartTime() > 0);
        assertEquals(batchCommitContext.getStartTime(), batchCommitContext.getFirstTime());
    }

    @Test
    void testAdd() {
        MessageBO message = MessageBO.builder()
            .queueId(0)
            .topic("TestTopic")
            .build();

        batchCommitContext.add(message);

        assertEquals(1, batchCommitContext.getSendMap().size());
        assertEquals(message, batchCommitContext.getSendMap().get(0));
    }

    @Test
    void testAddMultiple() {
        MessageBO message1 = MessageBO.builder()
            .queueId(0)
            .topic("TestTopic")
            .build();
        MessageBO message2 = MessageBO.builder()
            .queueId(1)
            .topic("TestTopic")
            .build();

        batchCommitContext.add(message1);
        batchCommitContext.add(message2);

        assertEquals(2, batchCommitContext.getSendMap().size());
        assertEquals(message1, batchCommitContext.getSendMap().get(0));
        assertEquals(message2, batchCommitContext.getSendMap().get(1));
    }

    @Test
    void testUpdateFirstTime() {
        long currentFirstTime = batchCommitContext.getFirstTime();
        batchCommitContext.updateFirstTime(currentFirstTime - 1000);

        assertEquals(currentFirstTime - 1000, batchCommitContext.getFirstTime());
    }

    @Test
    void testUpdateFirstTimeNotEarlier() {
        long currentFirstTime = batchCommitContext.getFirstTime();
        batchCommitContext.updateFirstTime(currentFirstTime + 1000);

        assertEquals(currentFirstTime, batchCommitContext.getFirstTime());
    }

    @Test
    void testNoMessageEmpty() {
        assertTrue(batchCommitContext.noMessage());
    }

    @Test
    void testNoMessageNotEmpty() {
        MessageBO message = MessageBO.builder()
            .queueId(0)
            .topic("TestTopic")
            .build();
        batchCommitContext.add(message);

        assertFalse(batchCommitContext.noMessage());
    }

    @Test
    void testGetSendEntrySet() {
        MessageBO message = MessageBO.builder()
            .queueId(0)
            .topic("TestTopic")
            .build();
        batchCommitContext.add(message);

        Set<Map.Entry<Integer, MessageBO>> entrySet = batchCommitContext.getSendEntrySet();

        assertEquals(1, entrySet.size());
        Map.Entry<Integer, MessageBO> entry = entrySet.iterator().next();
        assertEquals(0, entry.getKey());
        assertEquals(message, entry.getValue());
    }

    @Test
    void testSettersAndGetters() {
        batchCommitContext.setOverflow(true);
        assertTrue(batchCommitContext.isOverflow());

        long newTime = System.currentTimeMillis() + 10000;
        batchCommitContext.setFirstTime(newTime);
        assertEquals(newTime, batchCommitContext.getFirstTime());
    }
}
