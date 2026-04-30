package cn.coderule.wolfmq.broker.domain.transaction.check.context;

import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CheckContextTest {

    private CheckContext checkContext;

    @BeforeEach
    void setUp() {
        checkContext = new CheckContext();
    }

    @Test
    void testDefaultConstructor() {
        assertNotNull(checkContext);
        assertNotNull(checkContext.getOperationOffsetList());
        assertNotNull(checkContext.getOffsetMap());
        assertNotNull(checkContext.getOperationMap());
        assertEquals(1, checkContext.getInvalidPrepareMessageCount());
        assertEquals(0, checkContext.getMessageCheckCount());
        assertEquals(0, checkContext.getRpcFailureCount());
    }

    @Test
    void testBuilder() {
        TransactionConfig config = new TransactionConfig();
        CheckContext context = CheckContext.builder()
            .transactionConfig(config)
            .prepareOffset(100)
            .operationOffset(200)
            .build();

        assertNotNull(context);
        assertEquals(config, context.getTransactionConfig());
        assertEquals(100, context.getPrepareOffset());
        assertEquals(200, context.getOperationOffset());
    }

    @Test
    void testIsOffsetValid() {
        checkContext.setPrepareCounter(100);
        checkContext.setOperationOffset(200);
        assertTrue(checkContext.isOffsetValid());
    }

    @Test
    void testIsOffsetValidNegativePrepare() {
        checkContext.setPrepareCounter(-1);
        checkContext.setOperationOffset(200);
        assertFalse(checkContext.isOffsetValid());
    }

    @Test
    void testIsOffsetValidNegativeOperation() {
        checkContext.setPrepareCounter(100);
        checkContext.setOperationOffset(-1);
        assertFalse(checkContext.isOffsetValid());
    }

    @Test
    void testIncreaseMessageCheckCount() {
        checkContext.increaseMessageCheckCount();
        assertEquals(1, checkContext.getMessageCheckCount());
        
        checkContext.increaseMessageCheckCount();
        assertEquals(2, checkContext.getMessageCheckCount());
    }

    @Test
    void testIncreaseInvalidPrepareMessageCount() {
        checkContext.increaseInvalidPrepareMessageCount();
        assertEquals(2, checkContext.getInvalidPrepareMessageCount());
    }

    @Test
    void testSetPrepareCounter() {
        checkContext.setPrepareCounter(100);
        assertEquals(100, checkContext.getPrepareCounter());
        assertEquals(100, checkContext.getNextPrepareOffset());
    }

    @Test
    void testIncreasePrepareCounter() {
        checkContext.setPrepareCounter(100);
        checkContext.increasePrepareCounter();
        assertEquals(101, checkContext.getPrepareCounter());
        assertEquals(101, checkContext.getNextPrepareOffset());
    }

    @Test
    void testInitOffset() {
        checkContext.setPrepareOffset(50);
        checkContext.initOffset(100);
        
        assertEquals(50, checkContext.getNextPrepareOffset());
        assertEquals(50, checkContext.getPrepareCounter());
        assertEquals(100, checkContext.getNextOperationOffset());
    }

    @Test
    void testIsTimeout() {
        checkContext.setStartTime(System.currentTimeMillis() - 2000);
        assertTrue(checkContext.isTimeout(1000));
    }

    @Test
    void testIsNotTimeout() {
        checkContext.setStartTime(System.currentTimeMillis());
        assertFalse(checkContext.isTimeout(5000));
    }

    @Test
    void testAddOperationOffset() {
        checkContext.addOperationOffset(100);
        checkContext.addOperationOffset(200);
        
        assertEquals(2, checkContext.getOperationOffsetList().size());
        assertTrue(checkContext.getOperationOffsetList().contains(100L));
        assertTrue(checkContext.getOperationOffsetList().contains(200L));
    }

    @Test
    void testContainsPrepareOffset() {
        checkContext.linkOffset(100, 200);
        assertTrue(checkContext.containsPrepareOffset(100));
        assertFalse(checkContext.containsPrepareOffset(999));
    }

    @Test
    void testRemovePrepareOffset() {
        checkContext.linkOffset(100, 200);
        checkContext.removePrepareOffset(100);
        
        assertFalse(checkContext.containsPrepareOffset(100));
        assertTrue(checkContext.getOperationOffsetList().contains(200L));
    }

    @Test
    void testPutOffsetMap() {
        Set<Long> prepareOffsets = new HashSet<>();
        prepareOffsets.add(100L);
        prepareOffsets.add(101L);
        
        checkContext.putOffsetMap(200L, prepareOffsets);
        
        assertEquals(prepareOffsets, checkContext.getOperationMap().get(200L));
    }

    @Test
    void testCalculateOperationOffset() {
        checkContext.setOperationOffset(100);
        checkContext.addOperationOffset(100);
        checkContext.addOperationOffset(101);
        
        long result = checkContext.calculateOperationOffset();
        assertEquals(102, result);
    }

    @Test
    void testCalculateOperationOffsetWithGap() {
        checkContext.setOperationOffset(100);
        checkContext.addOperationOffset(100);
        checkContext.addOperationOffset(102);
        
        long result = checkContext.calculateOperationOffset();
        assertEquals(101, result);
    }
}
