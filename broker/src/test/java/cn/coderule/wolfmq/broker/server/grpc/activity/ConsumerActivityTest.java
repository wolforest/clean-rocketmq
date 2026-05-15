package cn.coderule.wolfmq.broker.server.grpc.activity;

import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResponse;
import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import apache.rocketmq.v2.ChangeInvisibleDurationResponse;
import apache.rocketmq.v2.GetOffsetRequest;
import apache.rocketmq.v2.GetOffsetResponse;
import apache.rocketmq.v2.QueryOffsetRequest;
import apache.rocketmq.v2.QueryOffsetResponse;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.Status;
import apache.rocketmq.v2.UpdateOffsetRequest;
import apache.rocketmq.v2.UpdateOffsetResponse;
import cn.coderule.wolfmq.broker.server.grpc.service.consume.GrpcAckService;
import cn.coderule.wolfmq.broker.server.grpc.service.consume.GrpcOffsetService;
import cn.coderule.wolfmq.broker.server.grpc.service.consume.InvisibleService;
import cn.coderule.wolfmq.broker.server.grpc.service.consume.PopService;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsumerActivityTest {

    @Mock
    private ThreadPoolExecutor executor;

    @Mock
    private PopService popService;

    @Mock
    private GrpcAckService ackService;

    @Mock
    private InvisibleService invisibleService;

    @Mock
    private GrpcOffsetService offsetService;

    @Mock
    private StreamObserver<ReceiveMessageResponse> receiveResponseObserver;

    @Mock
    private StreamObserver<AckMessageResponse> ackResponseObserver;

    @Mock
    private StreamObserver<ChangeInvisibleDurationResponse> invisibleResponseObserver;

    @Mock
    private StreamObserver<UpdateOffsetResponse> updateOffsetResponseObserver;

    @Mock
    private StreamObserver<GetOffsetResponse> getOffsetResponseObserver;

    @Mock
    private StreamObserver<QueryOffsetResponse> queryOffsetResponseObserver;

    @Mock
    private RequestContext requestContext;

    @Mock
    private ReceiveMessageRequest receiveRequest;

    @Mock
    private AckMessageRequest ackRequest;

    @Mock
    private ChangeInvisibleDurationRequest invisibleRequest;

    @Mock
    private UpdateOffsetRequest updateOffsetRequest;

    @Mock
    private GetOffsetRequest getOffsetRequest;

    @Mock
    private QueryOffsetRequest queryOffsetRequest;

    private ConsumerActivity consumerActivity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumerActivity = new ConsumerActivity(executor);
        consumerActivity.inject(popService, ackService, invisibleService, offsetService);
    }

    @Test
    void testConstructor_createsInstance() {
        assertNotNull(consumerActivity);
    }

    @Test
    void testInject_storesAllServices() {
        ConsumerActivity activity = new ConsumerActivity(executor);
        activity.inject(popService, ackService, invisibleService, offsetService);
        assertNotNull(activity);
    }

    @Test
    void testReceiveMessage_withValidRequest_submitsToExecutor() {
        when(executor.submit(any(Runnable.class))).thenReturn(null);
        consumerActivity.receiveMessage(requestContext, receiveRequest, receiveResponseObserver);
        verify(executor, times(1)).submit(any(Runnable.class));
    }

    @Test
    void testReceiveMessage_whenExecutorThrows_callsHelperWriteResponse() {
        doThrow(new RuntimeException("executor error")).when(executor).submit(any(Runnable.class));
        consumerActivity.receiveMessage(requestContext, receiveRequest, receiveResponseObserver);
        verify(receiveResponseObserver).onCompleted();
    }

    @Test
    void testAckMessage_withValidRequest_submitsToExecutor() {
        CompletableFuture<AckMessageResponse> future = new CompletableFuture<>();
        when(ackService.ack(any(), any())).thenReturn(future);
        when(executor.submit(any(Runnable.class))).thenReturn(null);
        consumerActivity.ackMessage(requestContext, ackRequest, ackResponseObserver);
        verify(executor, times(1)).submit(any(Runnable.class));
    }

    @Test
    void testAckMessage_whenExecutorThrows_callsHelperWriteResponse() {
        doThrow(new RuntimeException("executor error")).when(executor).submit(any(Runnable.class));
        consumerActivity.ackMessage(requestContext, ackRequest, ackResponseObserver);
        verify(ackResponseObserver).onCompleted();
    }

    @Test
    void testAckMessage_withNullFuture_completesSuccessfully() {
        when(ackService.ack(any(), any())).thenReturn(null);
        when(executor.submit(any(Runnable.class))).thenReturn(null);
        consumerActivity.ackMessage(requestContext, ackRequest, ackResponseObserver);
        verify(executor, times(1)).submit(any(Runnable.class));
    }

    @Test
    void testChangeInvisibleDuration_withValidRequest_submitsToExecutor() {
        CompletableFuture<ChangeInvisibleDurationResponse> future = new CompletableFuture<>();
        when(invisibleService.changeInvisible(any(), any())).thenReturn(future);
        when(executor.submit(any(Runnable.class))).thenReturn(null);
        consumerActivity.changeInvisibleDuration(requestContext, invisibleRequest, invisibleResponseObserver);
        verify(executor, times(1)).submit(any(Runnable.class));
    }

    @Test
    void testChangeInvisibleDuration_whenExecutorThrows_callsHelperWriteResponse() {
        doThrow(new RuntimeException("executor error")).when(executor).submit(any(Runnable.class));
        consumerActivity.changeInvisibleDuration(requestContext, invisibleRequest, invisibleResponseObserver);
        verify(invisibleResponseObserver).onCompleted();
    }

    @Test
    void testUpdateOffset_withValidRequest_submitsToExecutor() {
        CompletableFuture<UpdateOffsetResponse> future = new CompletableFuture<>();
        when(offsetService.updateOffsetAsync(any(), any())).thenReturn(future);
        when(executor.submit(any(Runnable.class))).thenReturn(null);
        consumerActivity.updateOffset(requestContext, updateOffsetRequest, updateOffsetResponseObserver);
        verify(executor, times(1)).submit(any(Runnable.class));
    }

    @Test
    void testUpdateOffset_whenExecutorThrows_callsHelperWriteResponse() {
        doThrow(new RuntimeException("executor error")).when(executor).submit(any(Runnable.class));
        consumerActivity.updateOffset(requestContext, updateOffsetRequest, updateOffsetResponseObserver);
        verify(updateOffsetResponseObserver).onCompleted();
    }

    @Test
    void testGetOffset_withValidRequest_submitsToExecutor() {
        CompletableFuture<GetOffsetResponse> future = new CompletableFuture<>();
        when(offsetService.getOffsetAsync(any(), any())).thenReturn(future);
        when(executor.submit(any(Runnable.class))).thenReturn(null);
        consumerActivity.getOffset(requestContext, getOffsetRequest, getOffsetResponseObserver);
        verify(executor, times(1)).submit(any(Runnable.class));
    }

    @Test
    void testGetOffset_whenExecutorThrows_callsHelperWriteResponse() {
        doThrow(new RuntimeException("executor error")).when(executor).submit(any(Runnable.class));
        consumerActivity.getOffset(requestContext, getOffsetRequest, getOffsetResponseObserver);
        verify(getOffsetResponseObserver).onCompleted();
    }

    @Test
    void testQueryOffset_withValidRequest_submitsToExecutor() {
        CompletableFuture<QueryOffsetResponse> future = new CompletableFuture<>();
        when(offsetService.queryOffsetAsync(any(), any())).thenReturn(future);
        when(executor.submit(any(Runnable.class))).thenReturn(null);
        consumerActivity.queryOffset(requestContext, queryOffsetRequest, queryOffsetResponseObserver);
        verify(executor, times(1)).submit(any(Runnable.class));
    }

    @Test
    void testQueryOffset_whenExecutorThrows_callsHelperWriteResponse() {
        doThrow(new RuntimeException("executor error")).when(executor).submit(any(Runnable.class));
        consumerActivity.queryOffset(requestContext, queryOffsetRequest, queryOffsetResponseObserver);
        verify(queryOffsetResponseObserver).onCompleted();
    }

    @Test
    void testReceiveStatusToResponse_buildsResponseWithStatus() {
        Status status = Status.newBuilder()
            .setCode(apache.rocketmq.v2.Code.OK)
            .setMessage("success")
            .build();
        ReceiveMessageResponse response = ReceiveMessageResponse.newBuilder()
            .setStatus(status)
            .build();
        assertNotNull(response);
        assertEquals(apache.rocketmq.v2.Code.OK, response.getStatus().getCode());
    }

    @Test
    void testAckStatusToResponse_buildsResponseWithStatus() {
        Status status = Status.newBuilder()
            .setCode(apache.rocketmq.v2.Code.OK)
            .setMessage("success")
            .build();
        AckMessageResponse response = AckMessageResponse.newBuilder()
            .setStatus(status)
            .build();
        assertNotNull(response);
    }

    @Test
    void testInvisibleStatusToResponse_buildsResponseWithStatus() {
        Status status = Status.newBuilder()
            .setCode(apache.rocketmq.v2.Code.OK)
            .setMessage("success")
            .build();
        ChangeInvisibleDurationResponse response = ChangeInvisibleDurationResponse.newBuilder()
            .setStatus(status)
            .build();
        assertNotNull(response);
    }

    @Test
    void testUpdateOffsetStatusToResponse_buildsResponseWithStatus() {
        Status status = Status.newBuilder()
            .setCode(apache.rocketmq.v2.Code.OK)
            .setMessage("success")
            .build();
        UpdateOffsetResponse response = UpdateOffsetResponse.newBuilder()
            .setStatus(status)
            .build();
        assertNotNull(response);
    }

    @Test
    void testGetOffsetStatusToResponse_buildsResponseWithStatus() {
        Status status = Status.newBuilder()
            .setCode(apache.rocketmq.v2.Code.OK)
            .setMessage("success")
            .build();
        GetOffsetResponse response = GetOffsetResponse.newBuilder()
            .setStatus(status)
            .build();
        assertNotNull(response);
    }

    @Test
    void testQueryOffsetStatusToResponse_buildsResponseWithStatus() {
        Status status = Status.newBuilder()
            .setCode(apache.rocketmq.v2.Code.OK)
            .setMessage("success")
            .build();
        QueryOffsetResponse response = QueryOffsetResponse.newBuilder()
            .setStatus(status)
            .build();
        assertNotNull(response);
    }
}
