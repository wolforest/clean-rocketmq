package cn.coderule.wolfmq.store.domain.timer.service;

import cn.coderule.wolfmq.domain.domain.timer.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlackHoleTimerTest {

    @Test
    void addAndScan_ShouldReturnEmpty() {
        BlackHoleTimer timer = new BlackHoleTimer();
        assertFalse(timer.addTimer(TimerEvent.builder().build()));

        ScanResult result = timer.scan(1000);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

