package cn.coderule.wolfmq.broker.domain.timer.service;

import cn.coderule.wolfmq.broker.domain.timer.context.TimerContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TimerFactoryTest {

    @Test
    void testLifecycleDoesNotThrow() {
        TimerContext context = mock(TimerContext.class);
        TimerFactory factory = new TimerFactory(context);

        assertDoesNotThrow(() -> factory.start());
        assertDoesNotThrow(() -> factory.shutdown());
    }
}