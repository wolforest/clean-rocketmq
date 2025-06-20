package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import apache.rocketmq.v2.Settings;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.domain.domain.constant.MQVersion;
import cn.coderule.minimq.domain.domain.enums.code.LanguageCode;
import cn.coderule.minimq.domain.domain.model.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;

public class TerminationService {

    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    private ProducerController producerController;
    private ConsumerController consumerController;

    public TerminationService(SettingManager settingManager, ChannelManager channelManager) {
        this.settingManager = settingManager;
        this.channelManager = channelManager;
    }

    public void inject(ProducerController producerController, ConsumerController consumerController) {
        this.producerController = producerController;
        this.consumerController = consumerController;
    }

    public CompletableFuture<NotifyClientTerminationResponse> terminate(RequestContext context, NotifyClientTerminationRequest request) {
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

    private CompletableFuture<NotifyClientTerminationResponse> process(
        RequestContext context,
        NotifyClientTerminationRequest request,
        Settings settings
    ) {
        switch (settings.getClientType()) {
            case PRODUCER -> unregisterProducer(context, request, settings);
            case SIMPLE_CONSUMER, PUSH_CONSUMER -> unregisterConsumer(context, request, settings);
            default -> notSupported(settings);
        };

        return success();
    }

    private void unregisterProducer(
        RequestContext context,
        NotifyClientTerminationRequest request,
        Settings settings
    ) {

    }

    private void unregisterConsumer(
        RequestContext context,
        NotifyClientTerminationRequest request,
        Settings settings
    ) {

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

    private ClientChannelInfo createChannelInfo(RequestContext context) {
        String clientId = context.getClientID();
        LanguageCode languageCode = LanguageCode.valueOf(context.getLanguage());
        GrpcChannel channel = channelManager.createChannel(context, clientId);
        int version = parseClientVersion(context.getClientVersion());

        return ClientChannelInfo.builder()
            .clientId(clientId)
            .channel(channel)
            .language(languageCode)
            .version(version)
            .lastUpdateTime(System.currentTimeMillis())
            .build();
    }

    private void notSupported(Settings settings) {
        CompletableFuture<NotifyClientTerminationResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.UNRECOGNIZED_CLIENT_TYPE, settings.getClientType().name());

        NotifyClientTerminationResponse response = NotifyClientTerminationResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

    }

    private CompletableFuture<NotifyClientTerminationResponse> success() {
        CompletableFuture<NotifyClientTerminationResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.OK, Code.OK.name());

        NotifyClientTerminationResponse response = NotifyClientTerminationResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

        return future;
    }

    private CompletableFuture<NotifyClientTerminationResponse> noSettings() {
        CompletableFuture<NotifyClientTerminationResponse> future = new CompletableFuture<>();
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.UNRECOGNIZED_CLIENT_TYPE, "can't find client settings");

        NotifyClientTerminationResponse response = NotifyClientTerminationResponse.newBuilder()
            .setStatus(status)
            .build();
        future.complete(response);

        return future;
    }

    private CompletableFuture<NotifyClientTerminationResponse> processError(Throwable t) {
        CompletableFuture<NotifyClientTerminationResponse> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }

}
