package cn.coderule.wolfmq.domain.domain.timer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerConstantsTest {

    @Test
    void testConstants() {
        assertEquals("rmq_sys_wheel_timer", TimerConstants.TIMER_TOPIC);
        assertEquals("CID_RMQ_SYS_TIMER_GROUP", TimerConstants.TIMER_GROUP);
        
        assertEquals(0, TimerConstants.INITIAL);
        assertEquals(1, TimerConstants.RUNNING);
        assertEquals(2, TimerConstants.HALT);
        assertEquals(3, TimerConstants.SHUTDOWN);
        
        assertEquals(1, TimerConstants.MAGIC_DEFAULT);
        assertEquals(2, TimerConstants.MAGIC_ROLL);
        assertEquals(4, TimerConstants.MAGIC_DELETE);
        
        assertEquals(0, TimerConstants.PUT_OK);
        assertEquals(1, TimerConstants.PUT_NEED_RETRY);
        assertEquals(2, TimerConstants.PUT_NO_RETRY);
        assertEquals(-1, TimerConstants.PUT_FAILED);
        
        assertEquals(60, TimerConstants.TIMER_BLANK_SLOTS);
    }
}