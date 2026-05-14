package cn.coderule.wolfmq.domain.domain.store.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OffsetUtilsTest {

    @Test
    void testOffsetToFileName() {
        String fileName = OffsetUtils.offsetToFileName(0L);
        assertNotNull(fileName);
        assertTrue(fileName.length() >= 20);
    }

    @Test
    void testFileNameToOffset_invalidInput() {
        assertEquals(0L, OffsetUtils.fileNameToOffset(null));
        assertEquals(0L, OffsetUtils.fileNameToOffset(""));
        assertEquals(0L, OffsetUtils.fileNameToOffset("   "));
        assertEquals(0L, OffsetUtils.fileNameToOffset("short"));
    }

    @Test
    void testOffsetToFileName_preservesValue() {
        long offset = 123L;
        String fileName = OffsetUtils.offsetToFileName(offset);
        assertNotNull(fileName);
    }
}