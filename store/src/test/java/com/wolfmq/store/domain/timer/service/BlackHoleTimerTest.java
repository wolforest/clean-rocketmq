package com.wolfmq.store.domain.timer.service;

import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
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

