package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.broker.server.bootstrap.RequestContext;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.Setter;

public class RouteActivity {
    private final ThreadPoolExecutor executor;

    @Setter
    private RouteController routeController;

    public RouteActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void getRoute(QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {
        QueryRouteResponse response = QueryRouteResponse.newBuilder()
            .addAllMessageQueues(new ArrayList<>())
            .setStatus(Status.newBuilder().setCode(Code.OK))
            .build();

        RequestContext context = new RequestContext();
        RouteInfo routeInfo = routeController.getRoute(context, request.getTopic().getName());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public void getAssignment(QueryAssignmentRequest request, StreamObserver<QueryAssignmentResponse> responseObserver) {
        QueryAssignmentResponse response = QueryAssignmentResponse.newBuilder()
            .setStatus(Status.newBuilder().setCode(Code.OK))
            .build();



        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
