package cn.coderule.wolfmq.rpc.common.rpc.protocol.code;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SystemResponseCodeTest {

    @Test
    void testSuccessCode() {
        assertEquals(0, SystemResponseCode.SUCCESS);
    }

    @Test
    void testSystemErrorCode() {
        assertEquals(1, SystemResponseCode.SYSTEM_ERROR);
    }

    @Test
    void testSystemBusyCode() {
        assertEquals(2, SystemResponseCode.SYSTEM_BUSY);
    }

    @Test
    void testRequestCodeNotSupported() {
        assertEquals(3, SystemResponseCode.REQUEST_CODE_NOT_SUPPORTED);
    }

    @Test
    void testTransactionFailed() {
        assertEquals(4, SystemResponseCode.TRANSACTION_FAILED);
    }

    @Test
    void testValuesAreUnique() {
        assertNotEquals(SystemResponseCode.SUCCESS, SystemResponseCode.SYSTEM_ERROR);
        assertNotEquals(SystemResponseCode.SYSTEM_ERROR, SystemResponseCode.SYSTEM_BUSY);
        assertNotEquals(SystemResponseCode.SYSTEM_BUSY, SystemResponseCode.REQUEST_CODE_NOT_SUPPORTED);
    }
}
