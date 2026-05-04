package cn.coderule.wolfmq.rpc.common.rpc.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SerializeTypeTest {

    @Test
    void jsonHasCodeZero() {
        assertEquals(0, SerializeType.JSON.getCode());
    }

    @Test
    void rocketmqHasCodeOne() {
        assertEquals(1, SerializeType.ROCKETMQ.getCode());
    }

    @Test
    void valueOfCodeZeroReturnsJson() {
        assertEquals(SerializeType.JSON, SerializeType.valueOf((byte) 0));
    }

    @Test
    void valueOfCodeOneReturnsRocketmq() {
        assertEquals(SerializeType.ROCKETMQ, SerializeType.valueOf((byte) 1));
    }

    @Test
    void valueOfUnknownCodeTwoReturnsNull() {
        assertNull(SerializeType.valueOf((byte) 2));
    }

    @Test
    void valueOfNegativeCodeReturnsNull() {
        assertNull(SerializeType.valueOf((byte) -1));
    }

    @Test
    void valueOfLargeCodeReturnsNull() {
        assertNull(SerializeType.valueOf((byte) 99));
    }
}