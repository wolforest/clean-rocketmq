package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.core.lock.queue.DequeueLock;
import cn.coderule.minimq.domain.domain.consumer.consume.InflightCounter;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.store.domain.meta.ConsumeOrderService;
import cn.coderule.minimq.domain.test.MessageMock;
import cn.coderule.minimq.store.domain.mq.ack.AckService;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DequeueServiceTest {

    @Test
    void dequeueReturnsFlowControlWhenInflightTooHigh() {
        MessageConfig messageConfig = new MessageConfig();
        messageConfig.setEnablePopThreshold(true);
        messageConfig.setPopInflightThreshold(1);
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setMessageConfig(messageConfig);

        DequeueLock dequeueLock = mock(DequeueLock.class);
        MessageService messageService = mock(MessageService.class);
        AckService ackService = mock(AckService.class);
        OffsetService offsetService = mock(OffsetService.class);
        InflightCounter inflightCounter = mock(InflightCounter.class);
        ConsumeOrderService orderService = mock(ConsumeOrderService.class);

        when(inflightCounter.get("TOPIC_A", "GROUP_A", 0)).thenReturn(2L);

        DequeueService service = new DequeueService(
            storeConfig,
            dequeueLock,
            messageService,
            ackService,
            offsetService,
            inflightCounter,
            orderService
        );

        DequeueRequest request = DequeueRequest.builder()
            .topicName("TOPIC_A")
            .consumerGroup("GROUP_A")
            .queueId(0)
            .build();

        DequeueResult result = service.dequeue(request);

        assertEquals(MessageStatus.FLOW_CONTROL, result.getStatus());
        verifyNoInteractions(dequeueLock, messageService, offsetService, ackService);
    }

    @Test
    void dequeueReturnsLockFailedWhenCannotLock() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setMessageConfig(new MessageConfig());

        DequeueLock dequeueLock = mock(DequeueLock.class);
        MessageService messageService = mock(MessageService.class);
        AckService ackService = mock(AckService.class);
        OffsetService offsetService = mock(OffsetService.class);
        InflightCounter inflightCounter = mock(InflightCounter.class);
        ConsumeOrderService orderService = mock(ConsumeOrderService.class);

        when(dequeueLock.tryLock(any())).thenReturn(false);

        DequeueService service = new DequeueService(
            storeConfig,
            dequeueLock,
            messageService,
            ackService,
            offsetService,
            inflightCounter,
            orderService
        );

        DequeueRequest request = DequeueRequest.builder()
            .topicName("TOPIC_B")
            .consumerGroup("GROUP_B")
            .queueId(1)
            .build();

        DequeueResult result = service.dequeue(request);

        assertEquals(MessageStatus.LOCK_FAILED, result.getStatus());
        verify(dequeueLock).tryLock(any());
        verifyNoInteractions(messageService, offsetService, ackService);
    }

    @Test
    void dequeueFetchesMessagesAndUpdatesCounters() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setMessageConfig(new MessageConfig());

        DequeueLock dequeueLock = mock(DequeueLock.class);
        MessageService messageService = mock(MessageService.class);
        AckService ackService = mock(AckService.class);
        OffsetService offsetService = mock(OffsetService.class);
        InflightCounter inflightCounter = mock(InflightCounter.class);
        ConsumeOrderService orderService = mock(ConsumeOrderService.class);

        when(dequeueLock.tryLock(any())).thenReturn(true);
        when(offsetService.getOffset(any())).thenReturn(5L);

        MessageBO first = MessageMock.createMessage("TOPIC_C", 0, 5);
        first.setStatus(MessageStatus.FOUND);
        MessageBO second = MessageMock.createMessage("TOPIC_C", 0, 6);
        second.setStatus(MessageStatus.FOUND);
        DequeueResult result = DequeueResult.success(List.of(first, second));
        when(messageService.get(any())).thenReturn(result);

        DequeueService service = new DequeueService(
            storeConfig,
            dequeueLock,
            messageService,
            ackService,
            offsetService,
            inflightCounter,
            orderService
        );

        DequeueRequest request = DequeueRequest.builder()
            .topicName("TOPIC_C")
            .consumerGroup("GROUP_C")
            .queueId(0)
            .build();

        service.dequeue(request);

        verify(offsetService).getOffset(any());
        verify(messageService).get(any());
        verify(offsetService).setNextOffset(any(), any());
        verify(inflightCounter).increment("TOPIC_C", "GROUP_C", 0, 2);
        verifyNoInteractions(ackService);
    }
}
