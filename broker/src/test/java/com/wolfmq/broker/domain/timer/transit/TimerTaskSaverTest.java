package com.wolfmq.broker.domain.timer.transit;

import com.wolfmq.broker.domain.timer.context.TimerContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerTaskSaverTest {

    @Test
    void testGetServiceName() {
        assertEquals("TimerTaskSaver", TimerTaskSaver.class.getSimpleName());
    }
}