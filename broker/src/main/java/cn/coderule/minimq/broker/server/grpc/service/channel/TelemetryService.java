package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.broker.grpc.ContextStreamObserver;
import cn.coderule.minimq.rpc.common.grpc.core.exception.GrpcException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelemetryService {

    public ContextStreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver) {
        return new ContextStreamObserver<>() {

            @Override
            public void onNext(RequestContext ctx, TelemetryCommand value) {
                try {
                    process(ctx, value, responseObserver);
                } catch (Throwable t) {
                    processException(value, t, responseObserver);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("telemetry error", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private void process(RequestContext ctx, TelemetryCommand command, StreamObserver<TelemetryCommand> responseObserver) {
        switch (command.getCommandCase()) {
            case SETTINGS -> processSettings(ctx, command, responseObserver);
            case THREAD_STACK_TRACE -> processTrace(ctx, command, responseObserver);
            case VERIFY_MESSAGE_RESULT -> processVerify(ctx, command, responseObserver);
        }
    }

    private void processSettings(RequestContext ctx, TelemetryCommand command, StreamObserver<TelemetryCommand> responseObserver) {
        Settings settings = command.getSettings();
    }

    private void processTrace(RequestContext ctx, TelemetryCommand command, StreamObserver<TelemetryCommand> responseObserver) {

    }

    private void processVerify(RequestContext ctx, TelemetryCommand command, StreamObserver<TelemetryCommand> responseObserver) {

    }

    private void processException(TelemetryCommand request, Throwable t,
        StreamObserver<TelemetryCommand> responseObserver) {
        StatusRuntimeException exception = initException(t);

        if (exception.getStatus().getCode().equals(io.grpc.Status.Code.INTERNAL)) {
            log.warn("process client telemetryCommand failed. request:{}", request, t);
        }
        responseObserver.onError(exception);
    }

    private StatusRuntimeException initGrpcException(Throwable t) {
        if (!(t instanceof GrpcException grpcException)) {
            return null;
        }

        int code = grpcException.getInvalidCode().getCode();
        if (code >= Code.INTERNAL_ERROR_VALUE || code < Code.BAD_REQUEST_VALUE) {
            return null;
        }

        return io.grpc.Status.INVALID_ARGUMENT
            .withDescription("process client telemetryCommand failed. " + t.getMessage())
            .withCause(t)
            .asRuntimeException();
    }

    private StatusRuntimeException initException(Throwable t) {
        StatusRuntimeException exception = initGrpcException(t);
        if (exception != null) {
            return exception;
        }

        return io.grpc.Status.INTERNAL
            .withDescription("process client telemetryCommand failed. " + t.getMessage())
            .withCause(t)
            .asRuntimeException();
    }
}
