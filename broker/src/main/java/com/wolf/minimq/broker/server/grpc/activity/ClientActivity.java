package com.wolf.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import apache.rocketmq.v2.TelemetryCommand;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientActivity {
    private final ThreadPoolExecutor executor;

    public ClientActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
    }

    public StreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver) {
        return null;
    }

    public void notifyClientTermination(
        NotifyClientTerminationRequest request, StreamObserver<NotifyClientTerminationResponse> responseObserver) {
    }
}
