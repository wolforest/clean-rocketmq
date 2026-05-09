package cn.coderule.wolfmq.broker.server.grpc.activity;

import apache.rocketmq.v2.Address;
import apache.rocketmq.v2.Endpoints;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.Resource;
import cn.coderule.wolfmq.broker.api.RouteController;
import cn.coderule.wolfmq.domain.config.network.GrpcConfig;
import cn.coderule.wolfmq.domain.core.enums.message.MessageType;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RouteActivityTest {

    @Mock
    private GrpcConfig grpcConfig;

    @Mock
    private ThreadPoolExecutor executor;

    @Mock
    private RouteController routeController;

    @Mock
    private StreamObserver<QueryRouteResponse> routeObserver;

    @Mock
    private StreamObserver<QueryAssignmentResponse> assignmentObserver;

    private RouteActivity activity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(grpcConfig.getPort()).thenReturn(8080);
        activity = new RouteActivity(grpcConfig, executor);
        activity.setRouteController(routeController);
    }

    @Test
    void testConstructor() {
        assertNotNull(activity);
    }

    @Test
    void testSetRouteController() {
        RouteActivity newActivity = new RouteActivity(grpcConfig, executor);
        newActivity.setRouteController(routeController);
        assertNotNull(newActivity);
    }

    @Test
    void testGetRoute_withValidRequest_submitsToExecutor() {
        RequestContext context = RequestContext.builder().build();
        Resource topicResource = Resource.newBuilder().setName("test-topic").build();
        Endpoints endpoints = Endpoints.newBuilder()
            .addAddresses(Address.newBuilder().setHost("127.0.0.1").setPort(8080).build())
            .build();
        QueryRouteRequest request = QueryRouteRequest.newBuilder()
            .setTopic(topicResource)
            .setEndpoints(endpoints)
            .build();

        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        activity.getRoute(context, request, routeObserver);

        verify(executor).submit(any(Runnable.class));
    }

    @Test
    void testGetRoute_executorThrowsException_handlesGracefully() {
        RequestContext context = RequestContext.builder().build();
        Resource topicResource = Resource.newBuilder().setName("test-topic").build();
        Endpoints endpoints = Endpoints.newBuilder()
            .addAddresses(Address.newBuilder().setHost("127.0.0.1").setPort(8080).build())
            .build();
        QueryRouteRequest request = QueryRouteRequest.newBuilder()
            .setTopic(topicResource)
            .setEndpoints(endpoints)
            .build();

        when(executor.submit(any(Runnable.class))).thenThrow(new RuntimeException("executor rejected"));

        activity.getRoute(context, request, routeObserver);

        verify(routeObserver).onError(any(Throwable.class));
    }

    @Test
    void testGetAssignment_withValidRequest_submitsToExecutor() {
        RequestContext context = RequestContext.builder().build();
        Resource topicResource = Resource.newBuilder().setName("test-topic").build();
        Resource groupResource = Resource.newBuilder().setName("test-group").build();
        Endpoints endpoints = Endpoints.newBuilder()
            .addAddresses(Address.newBuilder().setHost("127.0.0.1").setPort(8080).build())
            .build();
        QueryAssignmentRequest request = QueryAssignmentRequest.newBuilder()
            .setTopic(topicResource)
            .setGroup(groupResource)
            .setEndpoints(endpoints)
            .build();

        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(executor).submit(any(Runnable.class));

        activity.getAssignment(context, request, assignmentObserver);

        verify(executor).submit(any(Runnable.class));
    }

    @Test
    void testGetAssignment_executorThrowsException_handlesGracefully() {
        RequestContext context = RequestContext.builder().build();
        Resource topicResource = Resource.newBuilder().setName("test-topic").build();
        Resource groupResource = Resource.newBuilder().setName("test-group").build();
        Endpoints endpoints = Endpoints.newBuilder()
            .addAddresses(Address.newBuilder().setHost("127.0.0.1").setPort(8080).build())
            .build();
        QueryAssignmentRequest request = QueryAssignmentRequest.newBuilder()
            .setTopic(topicResource)
            .setGroup(groupResource)
            .setEndpoints(endpoints)
            .build();

        when(executor.submit(any(Runnable.class))).thenThrow(new RuntimeException("executor rejected"));

        activity.getAssignment(context, request, assignmentObserver);

        verify(assignmentObserver).onError(any(Throwable.class));
    }
}
