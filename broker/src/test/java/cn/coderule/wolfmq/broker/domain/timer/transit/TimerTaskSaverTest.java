package cn.coderule.wolfmq.broker.domain.timer.transit;

import cn.coderule.wolfmq.broker.domain.timer.context.TimerContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerTaskSaverTest {

    @Test
    void testGetServiceName() {
        assertEquals("TimerTaskSaver", TimerTaskSaver.class.getSimpleName());
    }
}