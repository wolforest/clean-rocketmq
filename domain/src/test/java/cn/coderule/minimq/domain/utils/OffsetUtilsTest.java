package cn.coderule.minimq.domain.utils;

import cn.coderule.minimq.domain.domain.store.utils.OffsetUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OffsetUtilsTest {

    @Test
    void offsetToFileName() {
        assertEquals("00000000000000000000", OffsetUtils.offsetToFileName(0));
        assertEquals("00000000000000000123", OffsetUtils.offsetToFileName(123));
        assertEquals("00000000000000012345", OffsetUtils.offsetToFileName(12345));
    }
}
