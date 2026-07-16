package cn.coderule.wolfmq.rpc.broker.rpc.protocol.body;

import cn.coderule.wolfmq.domain.core.enums.consume.CMResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsumeMessageDirectlyResultTest {

    @Test
    void gettersAndSetters() {
        ConsumeMessageDirectlyResult result = new ConsumeMessageDirectlyResult();
        result.setOrder(true);
        result.setAutoCommit(false);
        result.setConsumeResult(CMResult.CR_SUCCESS);
        result.setRemark("test");
        result.setSpentTimeMills(100L);

        assertTrue(result.isOrder());
        assertFalse(result.isAutoCommit());
        assertEquals(CMResult.CR_SUCCESS, result.getConsumeResult());
        assertEquals("test", result.getRemark());
        assertEquals(100L, result.getSpentTimeMills());
    }

    @Test
    void defaultValues() {
        ConsumeMessageDirectlyResult result = new ConsumeMessageDirectlyResult();
        assertFalse(result.isOrder());
        assertTrue(result.isAutoCommit());
        assertNull(result.getConsumeResult());
        assertNull(result.getRemark());
        assertEquals(0L, result.getSpentTimeMills());
    }

    @Test
    void toString_ShouldContainFields() {
        ConsumeMessageDirectlyResult result = new ConsumeMessageDirectlyResult();
        result.setConsumeResult(CMResult.CR_SUCCESS);
        String str = result.toString();
        assertTrue(str.contains("CR_SUCCESS"));
    }
}