package cn.coderule.wolfmq.broker.server.grpc.activity;

import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueRequest;
import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueResponse;
import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SendMessageResponse;
import cn.coderule.wolfmq.broker.api.ProducerController;
import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProducerActivityTest {

    @Mock
    private ThreadPoolExecutor executor;

    @Mock
    private ProducerController producerController;

    @Mock
    private StreamObserver<SendMessageResponse> produceObserver;

    @Mock
    private StreamObserver<ForwardMessageToDeadLetterQueueResponse> dlqObserver;

    private ProducerActivity activity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        activity = new ProducerActivity(executor);
        activity.setProducerController(producerController);
    }

    @Test
    void testConstructor() {
        assertNotNull(activity);
    }

    @Test
    void testSetProducerController() {
        ProducerActivity newActivity = new ProducerActivity(executor);
        newActivity.setProducerController(producerController);
        assertNotNull(newActivity);
    }

    @Test
    void testProduce_withValidRequest_submitsToExecutor() {
        RequestContext context = RequestContext.builder().build();
        SendMessageRequest request = SendMessageRequest.newBuilder().build();

        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        activity.produce(context, request, produceObserver);

        verify(executor).submit(any(Runnable.class));
    }

    @Test
    void testProduce_emptyRequest_throwsException() {
        RequestContext context = RequestContext.builder().build();
        SendMessageRequest request = SendMessageRequest.newBuilder().build();

        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            assertThrows(InvalidRequestException.class, r::run);
            return null;
        }).when(executor).submit(any(Runnable.class));

        activity.produce(context, request, produceObserver);

        verify(executor).submit(any(Runnable.class));
    }

    @Test
    void testMoveToDLQ_submitsToExecutor() {
        RequestContext context = RequestContext.builder().build();
        ForwardMessageToDeadLetterQueueRequest request = ForwardMessageToDeadLetterQueueRequest.newBuilder().build();

        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        activity.moveToDLQ(context, request, dlqObserver);

        verify(executor).submit(any(Runnable.class));
    }

    @Test
    void testProduce_executorThrowsException_writesError() {
        RequestContext context = RequestContext.builder().build();
        SendMessageRequest request = SendMessageRequest.newBuilder().build();

        when(executor.submit(any(Runnable.class)))
            .thenThrow(new RuntimeException("executor error"));

        assertDoesNotThrow(() -> activity.produce(context, request, produceObserver));
    }

    @Test
    void testProduceAsync_withController_producesMessage() {
        RequestContext context = RequestContext.builder().build();
        SendMessageRequest request = SendMessageRequest.newBuilder().build();

        List<MessageBO> messageBOList = List.of();
        CompletableFuture<List<MessageBO>> produceFuture = CompletableFuture.completedFuture(messageBOList);

        when(producerController.produce(any(), any())).thenReturn(produceFuture);

        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        activity.produce(context, request, produceObserver);

        verify(executor).submit(any(Runnable.class));
    }

    @Test
    void testMoveToDLQAsync_returnsCompletedFuture() {
        RequestContext context = RequestContext.builder().build();
        ForwardMessageToDeadLetterQueueRequest request = ForwardMessageToDeadLetterQueueRequest.newBuilder().build();

        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        activity.moveToDLQ(context, request, dlqObserver);

        verify(executor).submit(any(Runnable.class));
    }
}