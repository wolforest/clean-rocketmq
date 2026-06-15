package cn.coderule.wolfmq.broker.domain.timer.transit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerTaskSchedulerTest {

    @Test
    void testGetServiceName() {
        assertEquals("TimerTaskScheduler", TimerTaskScheduler.class.getSimpleName());
    }
}