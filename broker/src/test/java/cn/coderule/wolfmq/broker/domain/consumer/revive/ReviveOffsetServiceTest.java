package cn.coderule.wolfmq.broker.domain.consumer.revive;

import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.wolfmq.domain.domain.consumer.revive.ReviveBuffer;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.wolfmq.domain.domain.meta.offset.OffsetResult;
import cn.coderule.wolfmq.rpc.store.facade.ConsumeOffsetFacade;
import cn.coderule.wolfmq.rpc.store.facade.MQFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviveOffsetServiceTest {

    @Mock
    private ReviveContext reviveContext;

    @Mock
    private MQFacade mqFacade;

    @Mock
    private ConsumeOffsetFacade consumeOffsetFacade;

    private ReviveOffsetService offsetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(reviveContext.getReviveTopic()).thenReturn("reviveTopic");
        when(reviveContext.getMqFacade()).thenReturn(mqFacade);
        when(reviveContext.getConsumeOffsetFacade()).thenReturn(consumeOffsetFacade);
        offsetService = new ReviveOffsetService(reviveContext, 0);
    }

    @Test
    void testInitOffset() {
        OffsetResult offsetResult = new OffsetResult();
        offsetResult.setOffset(100L);
        when(consumeOffsetFacade.getOffset(any(OffsetRequest.class))).thenReturn(offsetResult);

        offsetService.initOffset();

        assertEquals(100L, offsetService.getReviveOffset());
    }

    @Test
    void testGetReviveDelayTime_NoTimestamp() {
        offsetService.setReviveTimestamp(-1);
        long delay = offsetService.getReviveDelayTime();
        assertEquals(0, delay);
    }

    @Test
    void testGetReviveDelayTime_WithMessages() {
        offsetService.setReviveTimestamp(System.currentTimeMillis() - 10000);
        offsetService.setReviveOffset(50);

        QueueResult queueResult = new QueueResult();
        queueResult.setMaxOffset(60);
        when(mqFacade.getMaxOffset(any(QueueRequest.class))).thenReturn(queueResult);

        long delay = offsetService.getReviveDelayTime();
        assertTrue(delay > 0);
    }

    @Test
    void testGetReviveDelayTime_NoMessages() {
        offsetService.setReviveTimestamp(System.currentTimeMillis());
        offsetService.setReviveOffset(50);

        QueueResult queueResult = new QueueResult();
        queueResult.setMaxOffset(51);
        when(mqFacade.getMaxOffset(any(QueueRequest.class))).thenReturn(queueResult);

        long delay = offsetService.getReviveDelayTime();
        assertEquals(0, delay);
    }

    @Test
    void testGetReviveDelayNumber() {
        offsetService.setReviveTimestamp(System.currentTimeMillis());
        offsetService.setReviveOffset(100);

        QueueResult queueResult = new QueueResult();
        queueResult.setMaxOffset(150);
        when(mqFacade.getMaxOffset(any(QueueRequest.class))).thenReturn(queueResult);

        long delayNumber = offsetService.getReviveDelayNumber();
        assertEquals(50, delayNumber);
    }

    @Test
    void testResetOffset() {
        ReviveBuffer buffer = new ReviveBuffer(100L);
        buffer.setOffset(200L);

        offsetService.resetOffset(buffer);

        assertEquals(200L, offsetService.getReviveOffset());
        verify(consumeOffsetFacade).putOffset(any(OffsetRequest.class));
    }

    @Test
    void testResetOffsetSkipRevive() {
        offsetService.setSkipRevive(true);
        ReviveBuffer buffer = new ReviveBuffer(100L);
        buffer.setOffset(200L);

        offsetService.resetOffset(buffer);

        verify(consumeOffsetFacade, never()).putOffset(any(OffsetRequest.class));
    }

    @Test
    void testResetOffsetInitialOffset() {
        ReviveBuffer buffer = new ReviveBuffer(100L);
        buffer.setOffset(100L);

        offsetService.resetOffset(buffer);

        verify(consumeOffsetFacade, never()).putOffset(any(OffsetRequest.class));
    }
}
