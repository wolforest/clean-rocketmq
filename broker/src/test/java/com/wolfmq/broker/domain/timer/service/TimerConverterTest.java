package com.wolfmq.broker.domain.timer.service;

import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TimerConverterTest {

    @Test
    void testToEvent() {
        MessageBO messageBO = mock(MessageBO.class);
        when(messageBO.getCommitOffset()).thenReturn(100L);
        when(messageBO.getMessageLength()).thenReturn(200);
        when(messageBO.getDelayTime()).thenReturn(5000L);

        TimerEvent event = TimerConverter.toEvent(messageBO, 0L, 1);
        assertNotNull(event);
        assertEquals(100L, event.getCommitLogOffset());
        assertEquals(200, event.getMessageSize());
        assertEquals(5000L, event.getDelayTime());
        assertEquals(1, event.getMagic());
    }
}
