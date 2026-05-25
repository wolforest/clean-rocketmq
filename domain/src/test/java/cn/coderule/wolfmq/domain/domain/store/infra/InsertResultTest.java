package cn.coderule.wolfmq.domain.domain.store.infra;

import cn.coderule.wolfmq.domain.core.enums.store.InsertStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsertResultTest {

    @Test
    void testSuccess() {
        InsertResult result = InsertResult.success(100L, 256);
        assertEquals(InsertStatus.PUT_OK, result.getStatus());
        assertEquals(100L, result.getWroteOffset());
        assertEquals(256, result.getWroteBytes());
        assertTrue(result.isSuccess());
        assertFalse(result.isEndOfFile());
    }

    @Test
    void testFailure() {
        InsertResult result = InsertResult.failure();
        assertEquals(InsertStatus.UNKNOWN_ERROR, result.getStatus());
        assertFalse(result.isSuccess());
        assertFalse(result.isEndOfFile());
    }

    @Test
    void testEndOfFile() {
        InsertResult result = InsertResult.endOfFile();
        assertEquals(InsertStatus.END_OF_FILE, result.getStatus());
        assertFalse(result.isSuccess());
        assertTrue(result.isEndOfFile());
    }

    @Test
    void testBuilder() {
        InsertResult result = InsertResult.builder()
            .status(InsertStatus.PUT_OK)
            .wroteOffset(200L)
            .wroteBytes(512)
            .storeTimestamp(System.currentTimeMillis())
            .build();

        assertEquals(200L, result.getWroteOffset());
        assertEquals(512, result.getWroteBytes());
        assertTrue(result.isSuccess());
    }

    @Test
    void testNoArgsConstructor() {
        InsertResult result = new InsertResult();
        assertNotNull(result);
    }
}