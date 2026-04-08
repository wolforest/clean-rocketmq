package cn.coderule.wolfmq.store.domain.mq.ack;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.wolfmq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.wolfmq.store.domain.mq.queue.EnqueueService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InvisibleServiceTest {

    @Test
    void changeInvisibleDelegatesToOffsetServiceWhenOrderly() {
        StoreConfig storeConfig = new StoreConfig();
        AckService ackService = mock(AckService.class);
        EnqueueService enqueueService = mock(EnqueueService.class);
        AckOffset offsetService = mock(AckOffset.class);

        AckResult expected = AckResult.success();
        when(offsetService.changeInvisible(any())).thenReturn(expected);

        InvisibleService service = new InvisibleService(
            storeConfig,
            "REVIVE_TOPIC",
            ackService,
            enqueueService,
            offsetService
        );

        AckMessage ackMessage = AckMessage.builder()
            .reviveQueueId(KeyBuilder.POP_ORDER_REVIVE_QUEUE)
            .ackInfo(AckInfo.builder()
                .topic("TOPIC_A")
                .consumerGroup("GROUP_A")
                .queueId(0)
                .ackOffset(5)
                .popTime(10)
                .startOffset(4)
                .brokerName("BROKER")
                .build())
            .build();

        AckResult actual = service.changeInvisible(ackMessage);
        assertEquals(expected, actual);
        verifyNoInteractions(ackService, enqueueService);
    }

    @Test
    void changeInvisibleAppendsCheckpointAndNacksForNonOrderly() {
        StoreConfig storeConfig = new StoreConfig();
        AckService ackService = mock(AckService.class);
        EnqueueService enqueueService = mock(EnqueueService.class);
        AckOffset offsetService = mock(AckOffset.class);

        when(enqueueService.enqueue(any())).thenReturn(new EnqueueResult(EnqueueStatus.PUT_OK));

        InvisibleService service = new InvisibleService(
            storeConfig,
            "REVIVE_TOPIC",
            ackService,
            enqueueService,
            offsetService
        );

        AckInfo ackInfo = AckInfo.builder()
            .topic("TOPIC_B")
            .consumerGroup("GROUP_B")
            .queueId(1)
            .ackOffset(7)
            .popTime(100)
            .startOffset(6)
            .brokerName("BROKER")
            .build();

        AckMessage ackMessage = AckMessage.builder()
            .reviveQueueId(1)
            .invisibleTime(30)
            .commitOffset(99)
            .receiptStr("receipt")
            .ackInfo(ackInfo)
            .build();

        service.changeInvisible(ackMessage);

        verify(enqueueService).enqueue(any());
        verify(ackService).nack(ackInfo, 1, 30);
        verifyNoInteractions(offsetService);
    }

    @Test
    void changeInvisibleReturnsFailureWhenCheckpointAppendFails() {
        StoreConfig storeConfig = new StoreConfig();
        AckService ackService = mock(AckService.class);
        EnqueueService enqueueService = mock(EnqueueService.class);
        AckOffset offsetService = mock(AckOffset.class);

        when(enqueueService.enqueue(any())).thenReturn(EnqueueResult.failure());

        InvisibleService service = new InvisibleService(
            storeConfig,
            "REVIVE_TOPIC",
            ackService,
            enqueueService,
            offsetService
        );

        AckMessage ackMessage = AckMessage.builder()
            .reviveQueueId(2)
            .invisibleTime(30)
            .commitOffset(99)
            .receiptStr("receipt")
            .ackInfo(AckInfo.builder()
                .topic("TOPIC_C")
                .consumerGroup("GROUP_C")
                .queueId(2)
                .ackOffset(7)
                .popTime(100)
                .startOffset(6)
                .brokerName("BROKER")
                .build())
            .build();

        AckResult result = service.changeInvisible(ackMessage);

        assertEquals(false, result.isSuccess());
        verify(enqueueService).enqueue(any());
        verifyNoInteractions(ackService, offsetService);
    }
}
