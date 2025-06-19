package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import apache.rocketmq.v2.Status;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.HeartbeatService;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.TelemetryService;
import cn.coderule.minimq.broker.server.grpc.service.channel.TerminationService;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.broker.grpc.ContextStreamObserver;
import cn.coderule.minimq.rpc.common.grpc.RequestPipeline;
import cn.coderule.minimq.rpc.common.grpc.activity.ActivityHelper;
import cn.coderule.minimq.rpc.common.grpc.core.constants.GrpcConstants;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientActivity {
    private final ThreadPoolExecutor executor;

    private ChannelManager channelManager;
    private SettingManager settingManager;
    private ConsumerController consumerController;

    private HeartbeatService heartbeatService;
    private TelemetryService telemetryService;
    private TerminationService terminationService;

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

    public void notifyClientTermination(RequestContext context, NotifyClientTerminationRequest request, StreamObserver<NotifyClientTerminationResponse> responseObserver) {
        ActivityHelper<NotifyClientTerminationRequest, NotifyClientTerminationResponse> helper = getTerminateHelper(context, request, responseObserver);
        try {
            Runnable task = () -> terminateAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public StreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver, RequestPipeline pipeline) {
        Function<Status, TelemetryCommand> statusToResponse = telemetryStatueToResponse();
        ContextStreamObserver<TelemetryCommand> response = telemetryAsync(responseObserver);

        return new StreamObserver<>() {
            @Override
            public void onNext(TelemetryCommand command) {
                RequestContext context = RequestContext.create();
                executePipeline(context, pipeline, command);

                ActivityHelper<TelemetryCommand, TelemetryCommand> helper = getTelemetryHelper(
                    context,
                    command,
                    responseObserver,
                    statusToResponse
                );


                execute(context, command, helper);
            }

            @Override
            public void onError(Throwable throwable) {
                response.onError(throwable);
            }

            @Override
            public void onCompleted() {
                response.onCompleted();
            }

            private void execute(RequestContext context, TelemetryCommand command, ActivityHelper<TelemetryCommand, TelemetryCommand> helper) {
                try {
                    Runnable task = () -> response.onNext(context, command);
                    ClientActivity.this.executor.submit(helper.createTask(task));
                } catch (Throwable t) {
                    helper.writeResponse(null, t);
                }
            }

            private void executePipeline(RequestContext context, RequestPipeline pipeline, TelemetryCommand request) {
                if (request != null) {
                    pipeline.execute(context, GrpcConstants.METADATA.get(Context.current()), request);
                    return;
                }

                log.error("[BUG] grpc request pipeline is not executed.");
            }
        };
    }

    private ContextStreamObserver<TelemetryCommand> telemetryAsync(StreamObserver<TelemetryCommand> responseObserver) {
        return telemetryService.telemetry(responseObserver);
    }

    private Function<Status, TelemetryCommand> telemetryStatueToResponse() {
        return status -> TelemetryCommand.newBuilder()
            .setStatus(status)
            .build();
    }

    private ActivityHelper<TelemetryCommand, TelemetryCommand> getTelemetryHelper(
        RequestContext context,
        TelemetryCommand request,
        StreamObserver<TelemetryCommand> responseObserver,
        Function<Status, TelemetryCommand> statusToResponse
    ) {
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private CompletableFuture<NotifyClientTerminationResponse> terminateAsync(RequestContext context, NotifyClientTerminationRequest request) {
        return terminationService.terminate(context, request);
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
}
