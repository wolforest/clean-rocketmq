package cn.coderule.wolfmq.store.domain.mq.ack;

import cn.coderule.wolfmq.domain.core.lock.queue.DequeueLock;
import cn.coderule.wolfmq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.wolfmq.domain.domain.consumer.consume.InflightCounter;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.ConsumeOrderService;
import cn.coderule.wolfmq.domain.domain.meta.topic.KeyBuilder;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AckOffsetTest {

    @Test
    void ackSkipsWhenNotOrderly() {
        DequeueLock dequeueLock = mock(DequeueLock.class);
        ConsumeOffsetService consumeOffsetService = mock(ConsumeOffsetService.class);
        ConsumeOrderService consumeOrderService = mock(ConsumeOrderService.class);
        InflightCounter inflightCounter = mock(InflightCounter.class);

        AckOffset offset = new AckOffset(dequeueLock, inflightCounter, consumeOffsetService, consumeOrderService);

        AckMessage ackMessage = AckMessage.builder()
            .reviveQueueId(1)
            .ackInfo(AckInfo.builder()
                .topic("TOPIC_A")
                .consumerGroup("GROUP_A")
                .queueId(0)
                .ackOffset(5)
                .popTime(10)
                .build())
            .build();

        offset.ack(ackMessage);

        verifyNoInteractions(dequeueLock, consumeOffsetService, consumeOrderService, inflightCounter);
    }

    @Test
    void ackSkipsWhenOffsetIsOld() {
        DequeueLock dequeueLock = mock(DequeueLock.class);
        ConsumeOffsetService consumeOffsetService = mock(ConsumeOffsetService.class);
        ConsumeOrderService consumeOrderService = mock(ConsumeOrderService.class);
        InflightCounter inflightCounter = mock(InflightCounter.class);

        when(consumeOffsetService.getOffset("GROUP_A", "TOPIC_A", 0)).thenReturn(10L);

        AckOffset offset = new AckOffset(dequeueLock, inflightCounter, consumeOffsetService, consumeOrderService);

        AckMessage ackMessage = AckMessage.builder()
            .reviveQueueId(KeyBuilder.POP_ORDER_REVIVE_QUEUE)
            .ackInfo(AckInfo.builder()
                .topic("TOPIC_A")
                .consumerGroup("GROUP_A")
                .queueId(0)
                .ackOffset(5)
                .popTime(10)
                .build())
            .build();

        offset.ack(ackMessage);

        verifyNoInteractions(dequeueLock, consumeOrderService, inflightCounter);
    }

    @Test
    void ackUpdatesOffsetAndDecrementsCounter() {
        DequeueLock dequeueLock = mock(DequeueLock.class);
        ConsumeOffsetService consumeOffsetService = mock(ConsumeOffsetService.class);
        ConsumeOrderService consumeOrderService = mock(ConsumeOrderService.class);
        InflightCounter inflightCounter = mock(InflightCounter.class);

        when(consumeOffsetService.getOffset("GROUP_A", "TOPIC_A", 0)).thenReturn(5L);
        when(consumeOffsetService.containsResetOffset("GROUP_A", "TOPIC_A", 0)).thenReturn(false);
        when(consumeOrderService.commit(any())).thenReturn(12L);

        AckOffset offset = new AckOffset(dequeueLock, inflightCounter, consumeOffsetService, consumeOrderService);

        AckMessage ackMessage = AckMessage.builder()
            .reviveQueueId(KeyBuilder.POP_ORDER_REVIVE_QUEUE)
            .ackInfo(AckInfo.builder()
                .topic("TOPIC_A")
                .consumerGroup("GROUP_A")
                .queueId(0)
                .ackOffset(5)
                .popTime(10)
                .build())
            .build();

        offset.ack(ackMessage);

        verify(dequeueLock).lock("TOPIC_A", "GROUP_A", 0);
        verify(dequeueLock).unlock("TOPIC_A", "GROUP_A", 0);
        verify(consumeOrderService).commit(any());
        verify(consumeOffsetService).putOffset("GROUP_A", "TOPIC_A", 0, 12L);
        verify(inflightCounter).decrement("TOPIC_A", "GROUP_A", 10L, 0, 1);
    }
}
