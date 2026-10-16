package cn.coderule.wolfmq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.Status;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.wolfmq.broker.server.grpc.interceptor.PipelineExecutor;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.rpc.broker.grpc.ContextStreamObserver;
import cn.coderule.wolfmq.rpc.common.grpc.activity.ActivityHelper;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelemetryObserver implements StreamObserver<TelemetryCommand> {
    private final ThreadPoolExecutor executor;
    private final PipelineExecutor pipelineExecutor;
    private final StreamObserver<TelemetryCommand> responseObserver;

    private final ContextStreamObserver<TelemetryCommand> response;
    private final Function<Status, TelemetryCommand> statusToResponse;

    public TelemetryObserver(
        PipelineExecutor pipelineExecutor,
        ThreadPoolExecutor executor,
        TelemetryService telemetryService,
        StreamObserver<TelemetryCommand> responseObserver
    ) {
        this.executor = executor;
        this.pipelineExecutor = pipelineExecutor;
        this.responseObserver = responseObserver;

        this.statusToResponse = telemetryStatueToResponse();
        this.response = telemetryService.telemetry(responseObserver);
    }

    @Override
    public void onNext(TelemetryCommand command) {
        RequestContext context = RequestContext.create();

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

    private void execute(
        RequestContext context,
        TelemetryCommand command,
        ActivityHelper<TelemetryCommand, TelemetryCommand> helper
    ) {
        try {
            pipelineExecutor.execute(context, command);

            Runnable task = () -> response.onNext(context, command);
            executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
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

    private Function<Status, TelemetryCommand> telemetryStatueToResponse() {
        return status -> TelemetryCommand.newBuilder()
            .setStatus(status)
            .build();
    }
}
