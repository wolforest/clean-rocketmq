package cn.coderule.wolfmq.broker.domain.timer.service;

import cn.coderule.wolfmq.broker.domain.timer.context.TimerContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CheckpointServiceTest {

    @Test
    void testGetServiceName() {
        TimerContext context = mock(TimerContext.class);
        CheckpointService service = new CheckpointService(context);
        assertEquals("CheckpointService", service.getServiceName());
    }
}