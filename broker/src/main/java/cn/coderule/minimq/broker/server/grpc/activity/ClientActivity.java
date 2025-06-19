package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.Status;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.HeartbeatService;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.activity.ActivityHelper;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

public class ClientActivity {
    private final ThreadPoolExecutor executor;

    private ChannelManager channelManager;
    private SettingManager settingManager;
    private ConsumerController consumerController;
    private HeartbeatService heartbeatService;

    public ClientActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void heartbeat(RequestContext context, HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        ActivityHelper<HeartbeatRequest, HeartbeatResponse> helper = getHeartbeatHelper(context, request, responseObserver);

        try {
            Runnable task = () -> heartbeatAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    private CompletableFuture<HeartbeatResponse> heartbeatAsync(RequestContext context, HeartbeatRequest request) {
        return heartbeatService.heartbeat(context, request);
    }

    private ActivityHelper<HeartbeatRequest, HeartbeatResponse> getHeartbeatHelper(
        RequestContext context,
        HeartbeatRequest request,
        StreamObserver<HeartbeatResponse> responseObserver
    ) {
        Function<Status, HeartbeatResponse> statusToResponse = heartbeatStatueToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private Function<Status, HeartbeatResponse> heartbeatStatueToResponse() {
        return status -> HeartbeatResponse.newBuilder()
            .setStatus(status)
            .build();
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
