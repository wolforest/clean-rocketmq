package cn.coderule.wolfmq.domain.domain.transaction;

import cn.coderule.wolfmq.domain.core.constant.MQConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionUtilTest {

    @Test
    void buildOffsetKey() {
        assertEquals("100,", TransactionUtil.buildOffsetKey(100));
        assertEquals("0,", TransactionUtil.buildOffsetKey(0));
    }

    @Test
    void buildOperationTopic() {
        assertEquals("RMQ_SYS_TRANS_OP_HALF_TOPIC", TransactionUtil.buildOperationTopic());
    }

    @Test
    void buildPrepareTopic() {
        assertEquals("RMQ_SYS_TRANS_HALF_TOPIC", TransactionUtil.buildPrepareTopic());
    }

    @Test
    void buildDiscardTopic() {
        assertEquals("TRANS_CHECK_MAX_TIME_TOPIC", TransactionUtil.buildDiscardTopic());
    }

    @Test
    void buildConsumerGroup() {
        assertEquals(MQConstants.CID_SYS_RMQ_TRANS, TransactionUtil.buildConsumerGroup());
    }

    @Test
    void getImmunityTime_customGreaterThanTimeout() {
        long result = TransactionUtil.getImmunityTime("30", 10_000);
        assertEquals(30_000, result);
    }

    @Test
    void getImmunityTime_customLessThanTimeout() {
        long result = TransactionUtil.getImmunityTime("5", 10_000);
        assertEquals(10_000, result);
    }

    @Test
    void getImmunityTime_invalidString_defaultsToTimeout() {
        long result = TransactionUtil.getImmunityTime("invalid", 10_000);
        assertEquals(10_000, result);
    }

    @Test
    void getImmunityTime_zeroCustom_defaultsToTimeout() {
        long result = TransactionUtil.getImmunityTime("0", 10_000);
        assertEquals(10_000, result);
    }
}