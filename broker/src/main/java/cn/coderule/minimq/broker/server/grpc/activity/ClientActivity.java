package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import apache.rocketmq.v2.Status;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;

public class ClientActivity {
    private final ThreadPoolExecutor executor;

    public ClientActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void heartbeat(RequestContext context, HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        Status status = Status.newBuilder()
            .setCode(Code.OK)
            .setMessage(Code.OK.name())
            .build();
        HeartbeatResponse response = HeartbeatResponse.newBuilder()
            .setStatus(status)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public StreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver) {
        return responseObserver;
    }

    public void notifyClientTermination(RequestContext context, NotifyClientTerminationRequest request, StreamObserver<NotifyClientTerminationResponse> responseObserver) {
        Status status = Status.newBuilder()
            .setCode(Code.OK)
            .setMessage(Code.OK.name())
            .build();
        NotifyClientTerminationResponse response = NotifyClientTerminationResponse.newBuilder()
            .setStatus(status)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
