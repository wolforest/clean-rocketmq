package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import apache.rocketmq.v2.Status;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.minimq.broker.server.grpc.service.channel.HeartbeatService;
import cn.coderule.minimq.broker.server.grpc.service.channel.TelemetryObserver;
import cn.coderule.minimq.broker.server.grpc.service.channel.TelemetryService;
import cn.coderule.minimq.broker.server.grpc.service.channel.TerminationService;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.RequestPipeline;
import cn.coderule.minimq.rpc.common.grpc.activity.ActivityHelper;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientActivity {
    private final ThreadPoolExecutor executor;

    private final HeartbeatService heartbeatService;
    private final TelemetryService telemetryService;
    private final TerminationService terminationService;

    public ClientActivity(
        ThreadPoolExecutor executor,
        HeartbeatService heartbeatService,
        TelemetryService telemetryService,
        TerminationService terminationService
    ) {
        this.executor = executor;

        this.heartbeatService = heartbeatService;
        this.telemetryService = telemetryService;
        this.terminationService = terminationService;
    }

    public void heartbeat(RequestContext context, HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        ActivityHelper<HeartbeatRequest, HeartbeatResponse> helper = getHeartbeatHelper(context, request, responseObserver);

        try {
            Runnable task = () -> heartbeatService.heartbeat(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public void notifyClientTermination(RequestContext context, NotifyClientTerminationRequest request, StreamObserver<NotifyClientTerminationResponse> responseObserver) {
        ActivityHelper<NotifyClientTerminationRequest, NotifyClientTerminationResponse> helper = getTerminateHelper(context, request, responseObserver);
        try {
            Runnable task = () -> terminationService.terminate(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public StreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver, RequestPipeline pipeline) {
        return new TelemetryObserver(
            pipeline,
            executor,
            telemetryService,
            responseObserver
        );
    }

    private ActivityHelper<NotifyClientTerminationRequest, NotifyClientTerminationResponse> getTerminateHelper(
        RequestContext context,
        NotifyClientTerminationRequest request,
        StreamObserver<NotifyClientTerminationResponse> responseObserver
    ) {
        Function<Status, NotifyClientTerminationResponse> statusToResponse = terminateStatueToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private Function<Status, NotifyClientTerminationResponse> terminateStatueToResponse() {
        return status -> NotifyClientTerminationResponse.newBuilder()
            .setStatus(status)
            .build();
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
}
