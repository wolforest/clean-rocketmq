package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.Resource;
import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.domain.domain.constant.MQVersion;
import cn.coderule.minimq.domain.domain.enums.code.LanguageCode;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class HeartbeatService {
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    private ProducerController producerController;
    private ConsumerController consumerController;

    public HeartbeatService(SettingManager settingManager, ChannelManager channelManager, RelayService relayService) {
        this.settingManager = settingManager;
        this.channelManager = channelManager;
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
            case PRODUCER -> registerProducer(context, request, settings);
            case SIMPLE_CONSUMER, PUSH_CONSUMER -> registerConsumer(context, request, settings);
            default -> notSupported(settings);
        };

        return success();
    }

    private void registerProducer(RequestContext context, HeartbeatRequest request, Settings settings) {
        for (Resource topic : settings.getPublishing().getTopicsList()) {
            String topicName = topic.getName();
            registerProducer(context, topicName);
        }
    }

    private void registerProducer(RequestContext context, String topicName) {
        String clientId = context.getClientID();
        LanguageCode languageCode = LanguageCode.valueOf(context.getLanguage());
        GrpcChannel channel = channelManager.createChannel(context, clientId);
        int version = parseClientVersion(context.getClientVersion());

        ClientChannelInfo channelInfo = ClientChannelInfo.builder()
            .clientId(clientId)
            .channel(channel)
            .language(languageCode)
            .version(version)
            .lastUpdateTime(System.currentTimeMillis())
            .build();

        producerController.register(context, topicName, channelInfo);

        // todo: add transaction subscription
    }

    private void registerConsumer(RequestContext context, HeartbeatRequest request, Settings settings) {
        String clientId = context.getClientID();
        LanguageCode languageCode = LanguageCode.valueOf(context.getLanguage());
        GrpcChannel channel = channelManager.createChannel(context, clientId);
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

    private int parseClientVersion(String clientVersionStr) {
        int clientVersion = MQVersion.CURRENT_VERSION;
        if (StringUtils.isEmpty(clientVersionStr)) {
            return clientVersion;
        }

        try {
            String tmp = StringUtils.upperCase(clientVersionStr);
            clientVersion = MQVersion.Version.valueOf(tmp).ordinal();
        } catch (Exception ignored) {
        }

        return clientVersion;
    }
}
