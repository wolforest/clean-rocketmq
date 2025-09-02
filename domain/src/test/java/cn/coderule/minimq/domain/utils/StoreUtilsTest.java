package cn.coderule.minimq.domain.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StoreUtilsTest {

    @Test
    void offsetToFileName() {
        assertEquals("00000000000000000000", StoreUtils.offsetToFileName(0));
        assertEquals("00000000000000000123", StoreUtils.offsetToFileName(123));
        assertEquals("00000000000000012345", StoreUtils.offsetToFileName(12345));
    }
}
