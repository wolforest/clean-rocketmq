package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.ClientType;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Resource;
import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.minimq.domain.core.enums.consume.CMResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.rpc.broker.grpc.ContextStreamObserver;
import cn.coderule.minimq.rpc.broker.rpc.protocol.body.ConsumeMessageDirectlyResult;
import cn.coderule.minimq.rpc.broker.rpc.protocol.body.ConsumerRunningInfo;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.core.relay.response.Result;
import cn.coderule.minimq.rpc.common.core.exception.RequestException;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.ResponseCode;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelemetryService {
    private final SettingManager settingManager;
    private final ChannelManager channelManager;
    private final RegisterService registerService;

    public TelemetryService(SettingManager settingManager, ChannelManager channelManager, RelayService relayService) {
        this.settingManager = settingManager;
        this.channelManager = channelManager;

        registerService = new RegisterService(channelManager);
    }

    public void inject(
        RouteController routeController,
        ProducerController producerController,
        ConsumerController consumerController,
        TransactionController transactionController
    ) {
        registerService.inject(routeController, producerController, consumerController, transactionController);
    }

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

    private void processSettings(RequestContext ctx, TelemetryCommand request, StreamObserver<TelemetryCommand> responseObserver) {
        Settings settings = request.getSettings();
        GrpcChannel channel = registerClient(ctx, responseObserver, settings);

        if (Settings.PubSubCase.PUBSUB_NOT_SET.equals(settings.getPubSubCase())) {
            responseObserver.onError(createInvalidException());
            return;
        }

        TelemetryCommand command = updateSettings(ctx, settings);
        if (command != null) {
            channel.writeTelemetryCommand(command);
            return;
        }

        responseObserver.onNext(null);
    }

    private StatusRuntimeException createInvalidException() {
        return Status.INVALID_ARGUMENT
            .withDescription("no publishing or subscription data in settings")
            .asRuntimeException();
    }

    protected TelemetryCommand updateSettings(RequestContext ctx, Settings settings) {
        String clientId = ctx.getClientID();
        settingManager.updateSettings(clientId, settings);
        Settings mergedSettings = settingManager.getSettings(ctx);

        return TelemetryCommand.newBuilder()
            .setStatus(ResponseBuilder.getInstance().buildStatus(Code.OK, Code.OK.name()))
            .setSettings(mergedSettings)
            .build();
    }


    private void processTrace(RequestContext ctx, TelemetryCommand request, StreamObserver<TelemetryCommand> responseObserver) {
        String nonce = request.getThreadStackTrace().getNonce();
        CompletableFuture<Result<ConsumerRunningInfo>> future = channelManager.getAndRemoveResult(nonce);
        if (future == null) {
            return;
        }

        apache.rocketmq.v2.Status status = request.getStatus();
        try {
            Result<ConsumerRunningInfo> result = systemError();

            if (status.getCode().equals(Code.VERIFY_FIFO_MESSAGE_UNSUPPORTED)) {
                 result = noPermission();
            }

            if (status.getCode().equals(Code.OK)) {
                result = success(request);
            }

            future.complete(result);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }

    private Result<ConsumerRunningInfo> success(TelemetryCommand request) {
        ConsumerRunningInfo info = new ConsumerRunningInfo();
        String threadStack = request.getThreadStackTrace().getThreadStackTrace();
        info.setJstack(threadStack);
        return new Result<>(
            ResponseCode.SUCCESS,
            "success",
            info
        );
    }

    private Result<ConsumerRunningInfo> systemError() {
        return new Result<>(
            ResponseCode.SYSTEM_ERROR,
            "verify message failed",
            null
        );
    }

    private Result<ConsumerRunningInfo> noPermission() {
        return new Result<>(
            ResponseCode.NO_PERMISSION,
            "forbidden to verify message",
            null
        );
    }

    private void processVerify(RequestContext ctx, TelemetryCommand request, StreamObserver<TelemetryCommand> responseObserver) {
        String nonce = request.getThreadStackTrace().getNonce();
        CompletableFuture<Result<ConsumeMessageDirectlyResult>> future = channelManager.getAndRemoveResult(nonce);
        if (future == null) {
            return;
        }

        try {
            ConsumeMessageDirectlyResult result = buildConsumeResult(request.getStatus());
            future.complete(new Result<>(
                ResponseCode.SUCCESS,
                "success",
                result
            ));
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
    }

    protected ConsumeMessageDirectlyResult buildConsumeResult(apache.rocketmq.v2.Status status) {
        ConsumeMessageDirectlyResult consumeMessageDirectlyResult = new ConsumeMessageDirectlyResult();
        switch (status.getCode().getNumber()) {
            case Code.OK_VALUE: {
                consumeMessageDirectlyResult.setConsumeResult(CMResult.CR_SUCCESS);
                break;
            }
            case Code.FAILED_TO_CONSUME_MESSAGE_VALUE: {
                consumeMessageDirectlyResult.setConsumeResult(CMResult.CR_LATER);
                break;
            }
            case Code.MESSAGE_CORRUPTED_VALUE: {
                consumeMessageDirectlyResult.setConsumeResult(CMResult.CR_RETURN_NULL);
                break;
            }
        }
        consumeMessageDirectlyResult.setRemark("from gRPC client");
        return consumeMessageDirectlyResult;
    }

    private GrpcChannel registerClient(RequestContext ctx, StreamObserver<TelemetryCommand> responseObserver, Settings settings) {
        GrpcChannel channel = null;
        switch (settings.getPubSubCase()) {
            case PUBLISHING:
                channel = registerProducer(ctx, responseObserver, settings);
                break;
            case SUBSCRIPTION:
                channel = registerConsumer(ctx, responseObserver, settings);
                break;
            default:
                break;
        }
        return channel;
    }

    private GrpcChannel registerProducer(RequestContext ctx, StreamObserver<TelemetryCommand> responseObserver, Settings settings) {
        GrpcChannel channel = null;

        for (Resource topic : settings.getPublishing().getTopicsList()) {
            String topicName = topic.getName();
            channel = registerService.registerProducer(ctx, topicName);
            channel.setClientObserver(responseObserver);
        }

        return channel;
    }

    private GrpcChannel registerConsumer(RequestContext ctx, StreamObserver<TelemetryCommand> responseObserver, Settings settings) {
        String groupName = settings.getSubscription().getGroup().getName();
        ClientType clientType = settings.getClientType();

        GrpcChannel channel = registerService.registerConsumer(
            ctx,
            groupName,
            clientType,
            settings,
            true
        );

        channel.setClientObserver(responseObserver);

        return channel;
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
        if (!(t instanceof RequestException requestException)) {
            return null;
        }

        int code = requestException.getInvalidCode().getCode();
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
