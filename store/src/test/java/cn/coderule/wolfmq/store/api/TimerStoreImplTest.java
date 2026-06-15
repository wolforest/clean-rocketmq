package cn.coderule.wolfmq.store.api;

import cn.coderule.wolfmq.domain.domain.timer.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.wolfmq.domain.domain.store.api.TimerStore;
import cn.coderule.wolfmq.store.domain.timer.service.TimerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerStoreImplTest {

    private TimerService timerService;
    private TimerStoreImpl store;

    @BeforeEach
    void setUp() {
        timerService = mock(TimerService.class);
        store = new TimerStoreImpl(timerService);
    }

    @Test
    void storeCheckpoint_ShouldDelegateToService() {
        TimerCheckpoint checkpoint = mock(TimerCheckpoint.class);

        store.storeCheckpoint(checkpoint);

        verify(timerService).storeCheckpoint(checkpoint);
    }

    @Test
    void getCheckpoint_ShouldDelegateToService() {
        TimerCheckpoint checkpoint = mock(TimerCheckpoint.class);
        when(timerService.loadCheckpoint()).thenReturn(checkpoint);

        TimerCheckpoint result = store.getCheckpoint();

        assertEquals(checkpoint, result);
        verify(timerService).loadCheckpoint();
    }

    @Test
    void addTimer_ShouldDelegateToService() {
        TimerEvent event = mock(TimerEvent.class);
        when(timerService.addTimer(event)).thenReturn(true);

        assertTrue(store.addTimer(event));
        verify(timerService).addTimer(event);
    }

    @Test
    void scan_ShouldDelegateToService() {
        ScanResult scanResult = mock(ScanResult.class);
        when(timerService.scan(1000L)).thenReturn(scanResult);

        ScanResult result = store.scan(1000L);

        assertEquals(scanResult, result);
        verify(timerService).scan(1000L);
    }

    @Test
    void getMetricJson_ShouldReturnEmpty() {
        assertEquals("", store.getMetricJson());
    }
}