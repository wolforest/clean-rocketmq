package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.Broker;
import apache.rocketmq.v2.Endpoints;
import apache.rocketmq.v2.MessageQueue;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.Status;
import cn.coderule.common.util.net.Address;
import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.broker.server.grpc.converter.RouteConverter;
import cn.coderule.minimq.domain.config.network.GrpcConfig;
import cn.coderule.minimq.domain.core.enums.message.MessageType;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.activity.ActivityHelper;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import lombok.Setter;

public class RouteActivity {
    private final GrpcConfig grpcConfig;
    private final ThreadPoolExecutor executor;

    @Setter
    private RouteController routeController;

    public RouteActivity(GrpcConfig grpcConfig, ThreadPoolExecutor executor) {
        this.grpcConfig = grpcConfig;
        this.executor = executor;
    }

    public void getRoute(RequestContext context, QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {
        ActivityHelper<QueryRouteRequest, QueryRouteResponse> helper = getRouteHelper(context, request, responseObserver);

        try {
            Runnable task = () -> getRouteAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public void getAssignment(RequestContext context, QueryAssignmentRequest request, StreamObserver<QueryAssignmentResponse> responseObserver) {
        ActivityHelper<QueryAssignmentRequest, QueryAssignmentResponse> helper = getAssignmentHelper(context, request, responseObserver);

        try {
            Runnable task = () -> getAssignmentAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    private ActivityHelper<QueryAssignmentRequest, QueryAssignmentResponse> getAssignmentHelper(
        RequestContext context,
        QueryAssignmentRequest request,
        StreamObserver<QueryAssignmentResponse> responseObserver
    ) {
        Function<Status, QueryAssignmentResponse> statusToResponse = assignmentStatueToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private ActivityHelper<QueryRouteRequest, QueryRouteResponse> getRouteHelper(
        RequestContext context,
        QueryRouteRequest request,
        StreamObserver<QueryRouteResponse> responseObserver
    ) {
        Function<Status, QueryRouteResponse> statusToResponse = routeStatueToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private CompletableFuture<QueryRouteResponse> getAssignmentAsync(RequestContext context, QueryAssignmentRequest request) {
        context.setServerPort(grpcConfig.getPort());
        context.setConsumeGroup(request.getGroup().getName());

        List<Address> addressList = toAddressList(request.getEndpoints());

        return routeController.getRoute(context, request.getTopic().getName(), addressList)
            .thenApply(routeInfo -> {
                return null;
            });
    }

    private CompletableFuture<QueryRouteResponse> getRouteAsync(RequestContext context, QueryRouteRequest request) {
        String topicName = request.getTopic().getName();
        List<Address> addressList = toAddressList(request.getEndpoints());
        context.setServerPort(grpcConfig.getPort());

        return routeController.getRoute(context, topicName, addressList)
            .thenApply(routeInfo -> {
                MessageType messageType = routeController.getTopicType(topicName);
                return RouteConverter.toRouteResponse(routeInfo, messageType, request);
            });
    }

    private List<Address> toAddressList(Endpoints endpoints) {
        List<Address> addressList = new ArrayList<>();
        for (apache.rocketmq.v2.Address address : endpoints.getAddressesList()) {
            addressList.add(Address.of(address.getHost(), grpcConfig.getPort()));
        }

        return addressList;
    }

    private Function<Status, QueryRouteResponse> routeStatueToResponse() {
        return status -> QueryRouteResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    private Function<Status, QueryAssignmentResponse> assignmentStatueToResponse() {
        return status -> QueryAssignmentResponse.newBuilder()
            .setStatus(status)
            .build();
    }
}
