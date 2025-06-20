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
        ClientChannelInfo channelInfo = createChannelInfo(context);

        producerController.register(context, topicName, channelInfo);

        // todo: add transaction subscription
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

    private void registerConsumer(RequestContext context, HeartbeatRequest request, Settings settings) {
        String consumerGroup = request.getGroup().getName();
        ClientChannelInfo channelInfo = createChannelInfo(context);

        Set<SubscriptionData> subscriptionDataSet = buildSubscriptionDataSet(
            settings.getSubscription().getSubscriptionsList()
        );

        ConsumerInfo consumerInfo = ConsumerInfo.builder()
            .groupName(consumerGroup)
            .messageModel(MessageModel.CLUSTERING)
            .consumeType(buildConsumeType(request.getClientType()))
            .consumeStrategy(ConsumeStrategy.CONSUME_FROM_LAST_OFFSET)
            .channelInfo(channelInfo)
            .subscriptionSet(subscriptionDataSet)
            .enableNotification(false)
            .enableSubscriptionModification(false)
            .build();

        consumerController.register(context, consumerInfo);
    }

    protected ConsumeType buildConsumeType(ClientType clientType) {
        return switch (clientType) {
            case SIMPLE_CONSUMER -> ConsumeType.CONSUME_ACTIVELY;
            case PUSH_CONSUMER -> ConsumeType.CONSUME_PASSIVELY;
            default -> throw new IllegalArgumentException(
                "Client type is not consumer, type: " + clientType);
        };
    }

    protected Set<SubscriptionData> buildSubscriptionDataSet(List<SubscriptionEntry> subscriptionEntryList) {
        Set<SubscriptionData> subscriptionDataSet = new HashSet<>();
        for (SubscriptionEntry sub : subscriptionEntryList) {
            String topicName = sub.getTopic().getName();
            FilterExpression filterExpression = sub.getExpression();
            subscriptionDataSet.add(buildSubscriptionData(topicName, filterExpression));
        }
        return subscriptionDataSet;
    }

    protected SubscriptionData buildSubscriptionData(String topicName, FilterExpression filterExpression) {
        String expression = filterExpression.getExpression();
        String expressionType = GrpcConverter.getInstance().buildExpressionType(filterExpression.getType());
        try {
            return FilterAPI.build(topicName, expression, expressionType);
        } catch (Exception e) {
            throw new GrpcException(InvalidCode.ILLEGAL_FILTER_EXPRESSION, "expression format is not correct", e);
        }
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
