package com.wolf.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;

public class RouteActivity {
    private final ThreadPoolExecutor executor;

    public RouteActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void queryRoute(QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {
    }
}
