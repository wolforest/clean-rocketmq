package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.enums.consume.ConsumeStrategy;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.domain.store.domain.meta.ConsumeOrderService;
import cn.coderule.minimq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.minimq.store.domain.mq.ack.AckService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OffsetServiceTest {

    @Test
    void getOffsetReturnsBufferedOffsetWhenHigher() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setMessageConfig(new MessageConfig());

        CommitLog commitLog = mock(CommitLog.class);
        AckService ackService = mock(AckService.class);
        ConsumeQueueManager consumeQueue = mock(ConsumeQueueManager.class);
        ConsumeOffsetService consumeOffsetService = mock(ConsumeOffsetService.class);
        ConsumeOrderService consumeOrderService = mock(ConsumeOrderService.class);

        when(consumeOffsetService.getOffset("GROUP_A", "TOPIC_A", 0)).thenReturn(5L);
        when(ackService.getBufferedOffset("GROUP_A", "TOPIC_A", 0)).thenReturn(12L);

        OffsetService service = new OffsetService(
            storeConfig,
            commitLog,
            ackService,
            consumeQueue,
            consumeOffsetService,
            consumeOrderService
        );

        DequeueRequest request = DequeueRequest.builder()
            .consumerGroup("GROUP_A")
            .topicName("TOPIC_A")
            .queueId(0)
            .build();

        long offset = service.getOffset(request);
        assertEquals(12L, offset);
        verifyNoInteractions(consumeQueue);
    }

    @Test
    void getOffsetUsesMinOffsetWhenConsumeFromStart() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setMessageConfig(new MessageConfig());

        CommitLog commitLog = mock(CommitLog.class);
        AckService ackService = mock(AckService.class);
        ConsumeQueueManager consumeQueue = mock(ConsumeQueueManager.class);
        ConsumeOffsetService consumeOffsetService = mock(ConsumeOffsetService.class);
        ConsumeOrderService consumeOrderService = mock(ConsumeOrderService.class);

        when(consumeOffsetService.getOffset("GROUP_B", "TOPIC_B", 1)).thenReturn(-1L);
        when(consumeQueue.getMinOffset("TOPIC_B", 1)).thenReturn(3L);
        when(ackService.getBufferedOffset("GROUP_B", "TOPIC_B", 1)).thenReturn(-1L);

        OffsetService service = new OffsetService(
            storeConfig,
            commitLog,
            ackService,
            consumeQueue,
            consumeOffsetService,
            consumeOrderService
        );

        DequeueRequest request = DequeueRequest.builder()
            .consumerGroup("GROUP_B")
            .topicName("TOPIC_B")
            .queueId(1)
            .consumeStrategy(ConsumeStrategy.CONSUME_FROM_START)
            .build();

        long offset = service.getOffset(request);
        assertEquals(3L, offset);
        verify(consumeOffsetService).getOffset("GROUP_B", "TOPIC_B", 1);
        verifyNoInteractions(consumeOrderService);
    }

    @Test
    void updateOffsetLocksOrderAndUpdatesConsumeOffset() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setMessageConfig(new MessageConfig());

        CommitLog commitLog = mock(CommitLog.class);
        AckService ackService = mock(AckService.class);
        ConsumeQueueManager consumeQueue = mock(ConsumeQueueManager.class);
        ConsumeOffsetService consumeOffsetService = mock(ConsumeOffsetService.class);
        ConsumeOrderService consumeOrderService = mock(ConsumeOrderService.class);

        OffsetService service = new OffsetService(
            storeConfig,
            commitLog,
            ackService,
            consumeQueue,
            consumeOffsetService,
            consumeOrderService
        );

        DequeueRequest request = DequeueRequest.builder()
            .consumerGroup("GROUP_C")
            .topicName("TOPIC_C")
            .queueId(2)
            .fifo(true)
            .build();

        DequeueResult result = new DequeueResult();
        result.setNextOffset(7);

        service.updateOffset(request, result);

        verify(consumeOrderService).lock(any());
        verify(consumeOffsetService).putOffset("GROUP_C", "TOPIC_C", 2, 7L);
    }
}
