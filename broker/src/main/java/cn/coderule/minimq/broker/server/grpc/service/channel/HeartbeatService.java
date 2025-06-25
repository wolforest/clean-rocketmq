package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.ClientType;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartbeatService {
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    private final RegisterService registerService;

    public HeartbeatService(SettingManager settingManager, ChannelManager channelManager, RelayService relayService) {
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

    public CompletableFuture<HeartbeatResponse> heartbeat(RequestContext context, HeartbeatRequest request) {
        try {
            Settings settings = settingManager.getSettings(context);
            if (settings == null) {
                return noSettings();
            }

            return process(context, request, settings);
        } catch (Throwable t) {
            return processError(t);
        }
    }

    private CompletableFuture<HeartbeatResponse> process(
        RequestContext context,
        HeartbeatRequest request,
        Settings settings
    ) {
        switch (request.getClientType()) {
            case PRODUCER -> registerService.registerProducer(context, settings);
            case SIMPLE_CONSUMER, PUSH_CONSUMER -> registerConsumer(context, request, settings);
            default -> notSupported(settings);
        };

        return success();
    }

    private void registerConsumer(
        RequestContext context,
        HeartbeatRequest request,
        Settings settings
    ) {
        String consumerGroup = request.getGroup().getName();
        ClientType clientType = request.getClientType();

        registerService.registerConsumer(
            context,
            consumerGroup,
            clientType,
            settings,
            false
        );
    }

    private void notSupported(Settings settings) {
        CompletableFuture<HeartbeatResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.UNRECOGNIZED_CLIENT_TYPE, settings.getClientType().name());

        HeartbeatResponse response = HeartbeatResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

    }

    private CompletableFuture<HeartbeatResponse> success() {
        CompletableFuture<HeartbeatResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.OK, Code.OK.name());

        HeartbeatResponse response = HeartbeatResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

        return future;
    }

    private CompletableFuture<HeartbeatResponse> noSettings() {
        CompletableFuture<HeartbeatResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.UNRECOGNIZED_CLIENT_TYPE, "can't find client settings");

        HeartbeatResponse response = HeartbeatResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

        return future;
    }

    private CompletableFuture<HeartbeatResponse> processError(Throwable t) {
        CompletableFuture<HeartbeatResponse> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }

}
