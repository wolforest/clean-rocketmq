package com.wolfmq.store.api;

import com.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import com.wolfmq.domain.domain.mq.dto.DequeueRequest;
import com.wolfmq.domain.domain.mq.dto.DequeueResult;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import com.wolfmq.domain.domain.mq.dto.EnqueueRequest;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import com.wolfmq.domain.domain.mq.MQService;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.store.domain.mq.ack.AckService;
import com.wolfmq.store.domain.mq.ack.InvisibleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MQStoreImplTest {

    private MQService mqService;
    private AckService ackService;
    private InvisibleService invisibleService;
    private ConsumeQueueManager consumeQueueManager;
    private MQStoreImpl store;

    @BeforeEach
    void setUp() {
        mqService = mock(MQService.class);
        ackService = mock(AckService.class);
        invisibleService = mock(InvisibleService.class);
        consumeQueueManager = mock(ConsumeQueueManager.class);
        store = new MQStoreImpl(mqService, ackService, invisibleService, consumeQueueManager);
    }

    @Test
    void enqueue_ShouldDelegateToService() {
        MessageBO messageBO = mock(MessageBO.class);
        EnqueueRequest request = mock(EnqueueRequest.class);
        EnqueueResult expectedResult = mock(EnqueueResult.class);
        when(request.getMessageBO()).thenReturn(messageBO);
        when(mqService.enqueue(messageBO)).thenReturn(expectedResult);

        EnqueueResult result = store.enqueue(request);

        assertEquals(expectedResult, result);
    }

    @Test
    void dequeue_ShouldDelegateToService() {
        DequeueRequest request = mock(DequeueRequest.class);
        DequeueResult expectedResult = mock(DequeueResult.class);
        when(mqService.dequeue(request)).thenReturn(expectedResult);

        DequeueResult result = store.dequeue(request);

        assertEquals(expectedResult, result);
    }

    @Test
    void get_ShouldDelegateToService() {
        DequeueRequest request = mock(DequeueRequest.class);
        DequeueResult expectedResult = mock(DequeueResult.class);
        when(mqService.get(request)).thenReturn(expectedResult);

        DequeueResult result = store.get(request);

        assertEquals(expectedResult, result);
    }

    @Test
    void getMessage_ShouldDelegateToService() {
        MessageRequest request = mock(MessageRequest.class);
        MessageResult expectedResult = mock(MessageResult.class);
        when(mqService.getMessage(request)).thenReturn(expectedResult);

        MessageResult result = store.getMessage(request);

        assertEquals(expectedResult, result);
    }

    @Test
    void getMinOffset_ShouldDelegateToConsumeQueueManager() {
        QueueRequest request = mock(QueueRequest.class);
        when(request.getTopicName()).thenReturn("t1");
        when(request.getQueueId()).thenReturn(0);
        when(consumeQueueManager.getMinOffset("t1", 0)).thenReturn(0L);

        QueueResult result = store.getMinOffset(request);
        assertNotNull(result);
    }

    @Test
    void getMaxOffset_ShouldDelegateToConsumeQueueManager() {
        QueueRequest request = mock(QueueRequest.class);
        when(request.getTopicName()).thenReturn("t1");
        when(request.getQueueId()).thenReturn(0);
        when(consumeQueueManager.getMaxOffset("t1", 0)).thenReturn(5000L);

        QueueResult result = store.getMaxOffset(request);
        assertNotNull(result);
    }
}
