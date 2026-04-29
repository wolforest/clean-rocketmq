package cn.coderule.wolfmq.broker.domain.consumer.pop;

import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PopServiceTest {

    @Mock
    private QueueSelector queueSelector;

    @Mock
    private ContextBuilder contextBuilder;

    @Mock
    private ReceiptHandler receiptHandler;

    @Mock
    private BrokerDequeueService dequeueService;

    @Mock
    private BrokerConfig brokerConfig;

    private PopService popService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        popService = new PopService(contextBuilder, queueSelector, dequeueService, receiptHandler);
        
        MessageConfig messageConfig = new MessageConfig();
        messageConfig.setPopRetryProbability(50);
        when(brokerConfig.getMessageConfig()).thenReturn(messageConfig);
    }

    @Test
    void testPopEmptyResult() {
        PopRequest request = createPopRequest("TestTopic", false);
        PopContext context = createPopContext(request);
        PopResult emptyResult = new PopResult();

        when(contextBuilder.build(request)).thenReturn(context);
        doNothing().when(queueSelector).select(context);
        when(dequeueService.dequeue(any(), anyString(), anyInt(), any()))
            .thenReturn(CompletableFuture.completedFuture(emptyResult));

        CompletableFuture<PopResult> future = popService.pop(request);
        PopResult result = future.join();

        assertNotNull(result);
    }

    @Test
    void testPopWithRequestQueueId() {
        PopRequest request = PopRequest.builder()
            .topicName("TestTopic")
            .queueId(2)
            .requestContext(new RequestContext())
            .build();
        PopContext context = createPopContext(request);

        PopResult emptyResult = new PopResult();

        when(contextBuilder.build(request)).thenReturn(context);
        doNothing().when(queueSelector).select(context);
        when(dequeueService.dequeue(any(), anyString(), eq(2), any()))
            .thenReturn(CompletableFuture.completedFuture(emptyResult));

        CompletableFuture<PopResult> future = popService.pop(request);
        PopResult result = future.join();

        assertNotNull(result);
        verify(dequeueService).dequeue(any(), anyString(), eq(2), any());
    }

    private PopRequest createPopRequest(String topicName, boolean fifo) {
        return PopRequest.builder()
            .topicName(topicName)
            .fifo(fifo)
            .requestContext(new RequestContext())
            .build();
    }

    private PopContext createPopContext(PopRequest request) {
        Topic topic = Topic.builder()
            .topicName(request.getTopicName())
            .readQueueNums(4)
            .writeQueueNums(4)
            .build();
        
        MessageQueue messageQueue = MessageQueue.builder()
            .topicName(request.getTopicName())
            .queueId(0)
            .build();

        PopContext context = new PopContext(brokerConfig, request);
        context.setTopic(topic);
        context.setMessageQueue(messageQueue);
        return context;
    }
}
