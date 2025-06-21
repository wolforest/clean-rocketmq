package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.ClientType;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.FilterExpression;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.Resource;
import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.Status;
import apache.rocketmq.v2.SubscriptionEntry;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.broker.server.grpc.converter.GrpcConverter;
import cn.coderule.minimq.domain.domain.constant.MQVersion;
import cn.coderule.minimq.domain.domain.dto.request.ConsumerInfo;
import cn.coderule.minimq.domain.domain.enums.code.InvalidCode;
import cn.coderule.minimq.domain.domain.enums.code.LanguageCode;
import cn.coderule.minimq.domain.domain.enums.consume.ConsumeStrategy;
import cn.coderule.minimq.domain.domain.enums.consume.ConsumeType;
import cn.coderule.minimq.domain.domain.enums.message.MessageModel;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.rpc.broker.core.FilterAPI;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.grpc.core.exception.GrpcException;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class HeartbeatService {
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    private final RegisterService registerService;

    private ProducerController producerController;
    private ConsumerController consumerController;

    public HeartbeatService(SettingManager settingManager, ChannelManager channelManager, RelayService relayService) {
        this.settingManager = settingManager;
        this.channelManager = channelManager;

        registerService = new RegisterService(channelManager);
    }

    public void inject(ProducerController producerController, ConsumerController consumerController) {
        this.producerController = producerController;
        this.consumerController = consumerController;

        registerService.inject(producerController, consumerController);
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
            case SIMPLE_CONSUMER, PUSH_CONSUMER -> {
                String consumerGroup = request.getGroup().getName();
                ClientType clientType = request.getClientType();
                registerService.registerConsumer(context, consumerGroup, clientType, settings);
            }
            default -> notSupported(settings);
        };

        return success();
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
