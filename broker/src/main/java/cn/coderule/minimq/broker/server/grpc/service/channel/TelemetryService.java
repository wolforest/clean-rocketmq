package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.ClientType;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Resource;
import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.broker.grpc.ContextStreamObserver;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.grpc.core.exception.GrpcException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelemetryService {
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    private final RegisterService registerService;

    private ProducerController producerController;
    private ConsumerController consumerController;

    public TelemetryService(SettingManager settingManager, ChannelManager channelManager, RelayService relayService) {
        this.settingManager = settingManager;
        this.channelManager = channelManager;

        registerService = new RegisterService(channelManager);
    }

    public void inject(ProducerController producerController, ConsumerController consumerController) {
        this.producerController = producerController;
        this.consumerController = consumerController;

        registerService.inject(producerController, consumerController);
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

    private void processSettings(RequestContext ctx, TelemetryCommand command, StreamObserver<TelemetryCommand> responseObserver) {
        Settings settings = command.getSettings();
        GrpcChannel channel = registerClient(ctx, responseObserver, settings);
    }

    private void processTrace(RequestContext ctx, TelemetryCommand command, StreamObserver<TelemetryCommand> responseObserver) {

    }

    private void processVerify(RequestContext ctx, TelemetryCommand command, StreamObserver<TelemetryCommand> responseObserver) {

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
