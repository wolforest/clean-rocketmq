package cn.coderule.wolfmq.broker.domain.consumer.pop;

import cn.coderule.wolfmq.broker.domain.meta.RouteService;
import cn.coderule.wolfmq.domain.config.business.TopicConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.core.exception.BrokerException;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.selector.MessageQueueSelector;
import cn.coderule.wolfmq.domain.domain.cluster.selector.MessageQueueView;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.wolfmq.domain.domain.meta.topic.KeyBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QueueSelectorTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private RouteService routeService;

    @Mock
    private TopicConfig topicConfig;

    @Mock
    private MessageQueueView queueView;

    @Mock
    private MessageQueueSelector readSelector;

    private QueueSelector queueSelector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(brokerConfig.getTopicConfig()).thenReturn(topicConfig);
        when(topicConfig.getReviveQueueNum()).thenReturn(8);
        queueSelector = new QueueSelector(brokerConfig, routeService);
    }

    @Test
    void testSelectWithStoreGroup() {
        String topicName = "TestTopic";
        String storeGroup = "broker1";
        
        PopRequest request = PopRequest.builder()
            .topicName(topicName)
            .storeGroup(storeGroup)
            .requestContext(new RequestContext())
            .build();

        PopContext context = new PopContext(brokerConfig, request);

        MessageQueue expectedQueue = MessageQueue.builder()
            .topicName(topicName)
            .groupName(storeGroup)
            .queueId(0)
            .build();

        when(routeService.getQueueView(any(), eq(topicName))).thenReturn(queueView);
        when(queueView.getReadSelector()).thenReturn(readSelector);
        when(readSelector.getQueueByBrokerName(storeGroup)).thenReturn(expectedQueue);

        queueSelector.select(context);

        assertEquals(expectedQueue, context.getMessageQueue());
    }

    @Test
    void testSelectWithoutStoreGroup() {
        String topicName = "TestTopic";
        
        PopRequest request = PopRequest.builder()
            .topicName(topicName)
            .requestContext(new RequestContext())
            .build();

        PopContext context = new PopContext(brokerConfig, request);

        MessageQueue expectedQueue = MessageQueue.builder()
            .topicName(topicName)
            .queueId(1)
            .build();

        when(routeService.getQueueView(any(), eq(topicName))).thenReturn(queueView);
        when(queueView.getReadSelector()).thenReturn(readSelector);
        when(readSelector.selectOne(true)).thenReturn(expectedQueue);

        queueSelector.select(context);

        assertEquals(expectedQueue, context.getMessageQueue());
    }

    @Test
    void testSelectQueueNotFound() {
        String topicName = "NonExistentTopic";
        
        PopRequest request = PopRequest.builder()
            .topicName(topicName)
            .requestContext(new RequestContext())
            .build();

        PopContext context = new PopContext(brokerConfig, request);

        when(routeService.getQueueView(any(), eq(topicName))).thenReturn(null);

        assertThrows(BrokerException.class, () -> queueSelector.select(context));
    }

    @Test
    void testSelectNoReadableQueue() {
        String topicName = "TestTopic";
        
        PopRequest request = PopRequest.builder()
            .topicName(topicName)
            .requestContext(new RequestContext())
            .build();

        PopContext context = new PopContext(brokerConfig, request);

        when(routeService.getQueueView(any(), eq(topicName))).thenReturn(queueView);
        when(queueView.getReadSelector()).thenReturn(readSelector);
        when(readSelector.selectOne(true)).thenReturn(null);

        assertThrows(BrokerException.class, () -> queueSelector.select(context));
    }

    @Test
    void testSelectReviveQueueFifo() {
        String topicName = "TestTopic";
        
        PopRequest request = PopRequest.builder()
            .topicName(topicName)
            .fifo(true)
            .requestContext(new RequestContext())
            .build();

        PopContext context = new PopContext(brokerConfig, request);

        MessageQueue queue = MessageQueue.builder()
            .topicName(topicName)
            .queueId(0)
            .build();

        when(routeService.getQueueView(any(), eq(topicName))).thenReturn(queueView);
        when(queueView.getReadSelector()).thenReturn(readSelector);
        when(readSelector.selectOne(true)).thenReturn(queue);

        queueSelector.select(context);

        assertEquals(KeyBuilder.POP_ORDER_REVIVE_QUEUE, context.getReviveQueueId());
    }

    @Test
    void testSelectReviveQueueNonFifo() {
        String topicName = "TestTopic";
        
        PopRequest request = PopRequest.builder()
            .topicName(topicName)
            .fifo(false)
            .requestContext(new RequestContext())
            .build();

        PopContext context = new PopContext(brokerConfig, request);

        MessageQueue queue = MessageQueue.builder()
            .topicName(topicName)
            .queueId(0)
            .build();

        when(routeService.getQueueView(any(), eq(topicName))).thenReturn(queueView);
        when(queueView.getReadSelector()).thenReturn(readSelector);
        when(readSelector.selectOne(true)).thenReturn(queue);

        queueSelector.select(context);

        assertTrue(context.getReviveQueueId() >= 0 && context.getReviveQueueId() < 8);
    }
}
