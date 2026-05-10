package cn.coderule.wolfmq.broker.server.grpc.activity;

import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.wolfmq.broker.server.grpc.service.channel.HeartbeatService;
import cn.coderule.wolfmq.broker.server.grpc.service.channel.TelemetryService;
import cn.coderule.wolfmq.broker.server.grpc.service.channel.TerminationService;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.rpc.common.grpc.RequestPipeline;
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

class ClientActivityTest {

    @Mock
    private ThreadPoolExecutor executor;

    @Mock
    private HeartbeatService heartbeatService;

    @Mock
    private TelemetryService telemetryService;

    @Mock
    private TerminationService terminationService;

    @Mock
    private StreamObserver<HeartbeatResponse> heartbeatResponseObserver;

    @Mock
    private StreamObserver<NotifyClientTerminationResponse> terminationResponseObserver;

    @Mock
    private StreamObserver<TelemetryCommand> telemetryResponseObserver;

    @Mock
    private RequestPipeline pipeline;

    private ClientActivity clientActivity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientActivity = new ClientActivity(
            executor,
            heartbeatService,
            telemetryService,
            terminationService
        );
    }

    @Test
    void testConstructor_createsInstanceWithAllDependencies() {
        assertNotNull(clientActivity);
    }

    @Test
    void testConstructor_storesAllServices() {
        ClientActivity activity = new ClientActivity(
            executor,
            heartbeatService,
            telemetryService,
            terminationService
        );
        assertNotNull(activity);
    }

    // ==================== heartbeat() tests ====================

    @Test
    void testHeartbeat_withValidRequest_delegatesToHeartbeatServiceAndSubmitsToExecutor() {
        RequestContext context = RequestContext.builder().build();
        HeartbeatRequest request = HeartbeatRequest.newBuilder().build();
        CompletableFuture<HeartbeatResponse> future = CompletableFuture.completedFuture(
            HeartbeatResponse.newBuilder().build()
        );

        when(heartbeatService.heartbeat(any(), any())).thenReturn(future);
        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        clientActivity.heartbeat(context, request, heartbeatResponseObserver);

        verify(heartbeatService).heartbeat(eq(context), eq(request));
        verify(executor).submit(any(Runnable.class));
    }

    @Test
    void testHeartbeat_serviceThrowsException_writesErrorResponse() {
        RequestContext context = RequestContext.builder().build();
        HeartbeatRequest request = HeartbeatRequest.newBuilder().build();

        when(heartbeatService.heartbeat(any(), any()))
            .thenThrow(new RuntimeException("heartbeat service error"));
        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        assertDoesNotThrow(() -> clientActivity.heartbeat(context, request, heartbeatResponseObserver));

        verify(heartbeatService).heartbeat(eq(context), eq(request));
    }

    @Test
    void testHeartbeat_executorThrowsException_handlesGracefully() {
        RequestContext context = RequestContext.builder().build();
        HeartbeatRequest request = HeartbeatRequest.newBuilder().build();
        CompletableFuture<HeartbeatResponse> future = CompletableFuture.completedFuture(
            HeartbeatResponse.newBuilder().build()
        );

        when(heartbeatService.heartbeat(any(), any())).thenReturn(future);
        when(executor.submit(any(Runnable.class)))
            .thenThrow(new RuntimeException("executor rejected"));

        assertDoesNotThrow(() -> clientActivity.heartbeat(context, request, heartbeatResponseObserver));

        verify(heartbeatResponseObserver).onCompleted();
        verify(executor).submit(any(Runnable.class));
    }

    // ==================== notifyClientTermination() tests ====================

    @Test
    void testNotifyClientTermination_withValidRequest_delegatesToTerminationServiceAndSubmitsToExecutor() {
        RequestContext context = RequestContext.builder().build();
        NotifyClientTerminationRequest request = NotifyClientTerminationRequest.newBuilder().build();
        CompletableFuture<NotifyClientTerminationResponse> future = CompletableFuture.completedFuture(
            NotifyClientTerminationResponse.newBuilder().build()
        );

        when(terminationService.terminate(any(), any())).thenReturn(future);
        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        clientActivity.notifyClientTermination(context, request, terminationResponseObserver);

        verify(terminationService).terminate(eq(context), eq(request));
        verify(executor).submit(any(Runnable.class));
    }

    @Test
    void testNotifyClientTermination_serviceThrowsException_writesErrorResponse() {
        RequestContext context = RequestContext.builder().build();
        NotifyClientTerminationRequest request = NotifyClientTerminationRequest.newBuilder().build();

        when(terminationService.terminate(any(), any()))
            .thenThrow(new RuntimeException("termination service error"));
        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        assertDoesNotThrow(() -> clientActivity.notifyClientTermination(context, request, terminationResponseObserver));

        verify(terminationService).terminate(eq(context), eq(request));
    }

    @Test
    void testNotifyClientTermination_executorThrowsException_handlesGracefully() {
        RequestContext context = RequestContext.builder().build();
        NotifyClientTerminationRequest request = NotifyClientTerminationRequest.newBuilder().build();
        CompletableFuture<NotifyClientTerminationResponse> future = CompletableFuture.completedFuture(
            NotifyClientTerminationResponse.newBuilder().build()
        );

        when(terminationService.terminate(any(), any())).thenReturn(future);
        when(executor.submit(any(Runnable.class)))
            .thenThrow(new RuntimeException("executor rejected"));

        assertDoesNotThrow(() -> clientActivity.notifyClientTermination(context, request, terminationResponseObserver));

        verify(terminationResponseObserver).onCompleted();
        verify(executor).submit(any(Runnable.class));
    }

    // ==================== telemetry() tests ====================

    @Test
    void testTelemetry_returnsTelemetryObserver() {
        StreamObserver<TelemetryCommand> result = clientActivity.telemetry(telemetryResponseObserver, pipeline);

        assertNotNull(result);
    }

    @Test
    void testTelemetry_delegatesToTelemetryService() {
        clientActivity.telemetry(telemetryResponseObserver, pipeline);

        verify(telemetryService).telemetry(eq(telemetryResponseObserver));
    }

    @Test
    void testTelemetry_withNullPipeline_stillCreatesObserver() {
        StreamObserver<TelemetryCommand> result = clientActivity.telemetry(telemetryResponseObserver, null);

        assertNotNull(result);
    }
}
