package cn.coderule.wolfmq.broker.domain.timer.transit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerTaskScannerTest {

    @Test
    void testGetServiceName() {
        assertEquals("TimerTaskScanner", TimerTaskScanner.class.getSimpleName());
    }
}