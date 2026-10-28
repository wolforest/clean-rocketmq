package com.wolfmq.broker.domain.consumer.revive;

import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.meta.offset.OffsetRequest;
import com.wolfmq.domain.domain.meta.offset.OffsetResult;
import com.wolfmq.domain.domain.meta.topic.TopicRequest;
import com.wolfmq.domain.domain.mq.dto.EnqueueRequest;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import com.wolfmq.domain.domain.mq.dto.EnqueueStatus;
import com.wolfmq.domain.core.mmap.InsertResult;
import com.wolfmq.rpc.store.facade.ConsumeOffsetFacade;
import com.wolfmq.rpc.store.facade.MQFacade;
import com.wolfmq.rpc.store.facade.TopicFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RetryServiceTest {

    @Mock
    private ReviveContext reviveContext;

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private MQFacade mqFacade;

    @Mock
    private TopicFacade topicFacade;

    @Mock
    private ConsumeOffsetFacade consumeOffsetFacade;

    @Mock
    private InsertResult mockInsertResult;

    private RetryService retryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(reviveContext.getBrokerConfig()).thenReturn(brokerConfig);
        when(reviveContext.getMqFacade()).thenReturn(mqFacade);
        when(reviveContext.getTopicFacade()).thenReturn(topicFacade);
        when(reviveContext.getConsumeOffsetFacade()).thenReturn(consumeOffsetFacade);
        when(brokerConfig.getHost()).thenReturn("127.0.0.1");
        when(brokerConfig.getPort()).thenReturn(10911);

        retryService = new RetryService(reviveContext);
    }

    @Test
    void testRetrySuccess() {
        PopCheckPoint point = createCheckPoint();
        MessageBO message = createMessage();

        when(topicFacade.exists(anyString())).thenReturn(true);
        when(consumeOffsetFacade.getOffset(any(OffsetRequest.class)))
            .thenReturn(OffsetResult.build(10));
        when(mqFacade.enqueue(any(EnqueueRequest.class)))
            .thenReturn(createSuccessResult(message));

        boolean result = retryService.retry(point, message);

        assertTrue(result);
        verify(mqFacade).enqueue(any(EnqueueRequest.class));
    }

    @Test
    void testRetryFailure() {
        PopCheckPoint point = createCheckPoint();
        MessageBO message = createMessage();

        when(topicFacade.exists(anyString())).thenReturn(true);
        when(consumeOffsetFacade.getOffset(any(OffsetRequest.class)))
            .thenReturn(OffsetResult.build(10));
        when(mqFacade.enqueue(any(EnqueueRequest.class)))
            .thenReturn(EnqueueResult.failure());

        boolean result = retryService.retry(point, message);

        assertFalse(result);
    }

    @Test
    void testRetryCreatesTopic() {
        PopCheckPoint point = createCheckPoint();
        MessageBO message = createMessage();

        when(topicFacade.exists(anyString())).thenReturn(false);
        doNothing().when(topicFacade).saveTopic(any(TopicRequest.class));
        when(consumeOffsetFacade.getOffset(any(OffsetRequest.class)))
            .thenReturn(OffsetResult.build(10));
        when(mqFacade.enqueue(any(EnqueueRequest.class)))
            .thenReturn(createSuccessResult(message));

        boolean result = retryService.retry(point, message);

        assertTrue(result);
        verify(topicFacade).saveTopic(any(TopicRequest.class));
    }

    @Test
    void testRetryInitsOffset() {
        PopCheckPoint point = createCheckPoint();
        MessageBO message = createMessage();

        when(topicFacade.exists(anyString())).thenReturn(true);
        when(consumeOffsetFacade.getOffset(any(OffsetRequest.class)))
            .thenReturn(OffsetResult.notFound());
        doNothing().when(consumeOffsetFacade).putOffset(any(OffsetRequest.class));
        when(mqFacade.enqueue(any(EnqueueRequest.class)))
            .thenReturn(createSuccessResult(message));

        boolean result = retryService.retry(point, message);

        assertTrue(result);
        verify(consumeOffsetFacade).putOffset(any(OffsetRequest.class));
    }

    @Test
    void testRetrySkipsOffsetInitIfExists() {
        PopCheckPoint point = createCheckPoint();
        MessageBO message = createMessage();

        when(topicFacade.exists(anyString())).thenReturn(true);
        when(consumeOffsetFacade.getOffset(any(OffsetRequest.class)))
            .thenReturn(OffsetResult.build(5));
        when(mqFacade.enqueue(any(EnqueueRequest.class)))
            .thenReturn(createSuccessResult(message));

        boolean result = retryService.retry(point, message);

        assertTrue(result);
        verify(consumeOffsetFacade, never()).putOffset(any(OffsetRequest.class));
    }

    private PopCheckPoint createCheckPoint() {
        return PopCheckPoint.builder()
            .topic("TestTopic")
            .cid("TestGroup")
            .queueId(0)
            .startOffset(100)
            .popTime(System.currentTimeMillis())
            .invisibleTime(30000)
            .build();
    }

    private MessageBO createMessage() {
        return MessageBO.builder()
            .topic("TestTopic")
            .messageId("msg123")
            .body("test body".getBytes())
            .storeGroup("broker1")
            .build();
    }

    private EnqueueResult createSuccessResult(MessageBO message) {
        return EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .insertResult(mockInsertResult)
            .storeGroup(message.getStoreGroup())
            .messageId(message.getMessageId())
            .build();
    }
}
