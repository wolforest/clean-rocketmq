package cn.coderule.wolfmq.broker.domain.timer.service;

import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SplitServiceTest {

    @Test
    void testSplitSmallList() {
        SplitService service = new SplitService(1024);
        List<TimerEvent> origin = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            origin.add(mock(TimerEvent.class));
        }

        List<List<TimerEvent>> result = service.split(origin);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testConstructor() {
        SplitService service = new SplitService(2048);
        assertEquals(0, service.getMsgIndex());
        assertEquals(-1, service.getFileIndex());
    }
}