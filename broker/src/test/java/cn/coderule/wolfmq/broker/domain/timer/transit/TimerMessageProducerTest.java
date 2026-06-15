package cn.coderule.wolfmq.broker.domain.timer.transit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerMessageProducerTest {

    @Test
    void testGetServiceName() {
        assertEquals("TimerMessageProducer", TimerMessageProducer.class.getSimpleName());
    }
}