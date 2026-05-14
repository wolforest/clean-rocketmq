package cn.coderule.wolfmq.domain.domain.timer;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerEventTest {

    @Test
    void testBuilder() {
        RequestContext ctx = RequestContext.builder().build();
        MessageBO msg = mock(MessageBO.class);
        
        TimerEvent event = TimerEvent.builder()
            .requestContext(ctx)
            .storeGroup("store1")
            .commitLogOffset(100L)
            .messageSize(256)
            .delayTime(1000L)
            .batchTime(0L)
            .magic(1)
            .enqueueTime(System.currentTimeMillis())
            .messageBO(msg)
            .released(false)
            .build();

        assertNotNull(event);
        assertEquals(ctx, event.getRequestContext());
        assertEquals("store1", event.getStoreGroup());
        assertEquals(100L, event.getCommitLogOffset());
        assertEquals(256, event.getMessageSize());
        assertEquals(1000L, event.getDelayTime());
        assertEquals(0L, event.getBatchTime());
        assertEquals(1, event.getMagic());
        assertEquals(msg, event.getMessageBO());
        assertFalse(event.isReleased());
    }

    @Test
    void testNoArgsConstructor() {
        TimerEvent event = new TimerEvent();
        assertNotNull(event);
    }

    @Test
    void testAllArgsConstructor() {
        RequestContext ctx = RequestContext.builder().build();
        MessageBO msg = mock(MessageBO.class);
        
        TimerEvent event = new TimerEvent();
        event.setRequestContext(ctx);
        event.setStoreGroup("store1");
        event.setCommitLogOffset(100L);
        event.setMessageSize(256);
        event.setReleased(false);

        assertEquals(ctx, event.getRequestContext());
        assertEquals("store1", event.getStoreGroup());
        assertEquals(100L, event.getCommitLogOffset());
        assertEquals(256, event.getMessageSize());
        assertFalse(event.isReleased());
    }
}