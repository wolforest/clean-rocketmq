package com.wolf.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import com.wolf.minimq.broker.api.RouteController;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.Setter;

public class RouteActivity {
    private final ThreadPoolExecutor executor;

    @Setter
    private RouteController routeController;

    public RouteActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void queryRoute(QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {
    }
}
