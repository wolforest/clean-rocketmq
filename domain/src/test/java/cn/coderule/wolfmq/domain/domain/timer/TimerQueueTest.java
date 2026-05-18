package cn.coderule.wolfmq.domain.domain.timer;

import cn.coderule.wolfmq.domain.config.business.TimerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerQueueTest {

    private TimerQueue timerQueue;

    @BeforeEach
    void setUp() {
        timerQueue = new TimerQueue(new TimerConfig());
    }

    @Test
    void testConstructor() {
        assertNotNull(timerQueue);
    }

    @Test
    void testQueueEmptyInitially() {
        assertTrue(timerQueue.isConsumeQueueEmpty());
        assertTrue(timerQueue.isProduceQueueEmpty());
        assertTrue(timerQueue.isScheduleQueueEmpty());
    }

    @Test
    void testConsumeQueue() throws InterruptedException {
        TimerEvent event = mock(TimerEvent.class);

        timerQueue.putConsumeEvent(event);
        assertFalse(timerQueue.isConsumeQueueEmpty());

        TimerEvent polled = timerQueue.pollConsumeEvent(100);
        assertEquals(event, polled);
        assertTrue(timerQueue.isConsumeQueueEmpty());
    }

    @Test
    void testProduceQueue() throws InterruptedException {
        TimerEvent event = mock(TimerEvent.class);

        timerQueue.putProduceEvent(event);
        assertFalse(timerQueue.isProduceQueueEmpty());
        assertTrue(timerQueue.offerProduceEvent(event, 100));

        TimerEvent polled = timerQueue.pollProduceEvent(100);
        assertEquals(event, polled);
    }

    @Test
    void testScheduleQueue() throws InterruptedException {
        TimerEvent event = mock(TimerEvent.class);
        List<TimerEvent> events = Collections.singletonList(event);

        timerQueue.putScheduleEvent(events);
        assertFalse(timerQueue.isScheduleQueueEmpty());

        List<TimerEvent> polled = timerQueue.pollScheduleEvent(100);
        assertEquals(events, polled);
    }
}