package cn.coderule.wolfmq.store.domain.commitlog.flush;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FlusherTest {

    @Test
    void testSetMaxOffsetOnlyIncreases() {
        TestFlusher flusher = new TestFlusher();
        flusher.setMaxOffset(100);
        assertEquals(100, flusher.getMaxOffsetValue());

        flusher.setMaxOffset(50);
        assertEquals(100, flusher.getMaxOffsetValue());

        flusher.setMaxOffset(200);
        assertEquals(200, flusher.getMaxOffsetValue());
    }

    @Test
    void testGetServiceName() {
        TestFlusher flusher = new TestFlusher();
        assertEquals("TestFlusher", flusher.getServiceName());
    }

    @Test
    void testAddRequestDoesNotThrow() {
        TestFlusher flusher = new TestFlusher();
        assertDoesNotThrow(() -> flusher.addRequest(null));
    }

    private static class TestFlusher extends Flusher {
        public String getServiceName() {
            return "TestFlusher";
        }

        public long getMaxOffsetValue() {
            return maxOffset;
        }

        @Override
        public void run() {
        }
    }
}
