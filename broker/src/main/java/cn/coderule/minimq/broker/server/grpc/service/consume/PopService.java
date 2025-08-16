package cn.coderule.minimq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.FilterExpression;
import apache.rocketmq.v2.MessageQueue;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.Settings;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.server.grpc.converter.GrpcConverter;
import cn.coderule.minimq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.domain.config.network.GrpcConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.enums.consume.ConsumeInitMode;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.rpc.broker.core.FilterAPI;
import com.google.protobuf.util.Durations;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopService {
    private final BrokerConfig brokerConfig;
    private final GrpcConfig grpcConfig;
    private final ConsumerController consumerController;
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    public PopService(
        BrokerConfig brokerConfig,
        ConsumerController consumerController,
        SettingManager settingManager,
        ChannelManager channelManager
    ) {
        this.brokerConfig = brokerConfig;
        this.grpcConfig = brokerConfig.getGrpcConfig();
        this.consumerController = consumerController;
        this.settingManager = settingManager;
        this.channelManager = channelManager;
    }

    public CompletableFuture<ReceiveMessageResponse> receive(
        RequestContext context,
        ReceiveMessageRequest request,
        StreamObserver<ReceiveMessageResponse> responseObserver
    ) {
        ConsumeResponse response = new ConsumeResponse(consumerController, responseObserver);
        Settings settings = settingManager.getSettings(context);
        if (settings == null) {
            return response.noSettings();
        }

        Long pollTime = getPollTime(context, request, settings);
        if (pollTime == null) {
            return response.notEnoughTime();
        }

        SubscriptionData subscriptionData = buildFilter(request);
        if (subscriptionData == null) {
            return response.illegalFilter();
        }

        PopRequest popRequest = buildPopRequest(context, request, settings, pollTime, subscriptionData);
        //return consumerController.popMessage(popRequest);

        return null;
    }

    private PopRequest buildPopRequest(
        RequestContext context,
        ReceiveMessageRequest request,
        Settings settings,
        Long pollTime,
        SubscriptionData subscriptionData
    ) {
        long invisibleTime = Durations.toMillis(request.getInvisibleDuration());
        MessageQueue mq = request.getMessageQueue();

        return PopRequest.builder()
            .requestContext(context)
            .storeGroup(mq.getBroker().getName())
            .consumerGroup(request.getGroup().getName())
            .topicName(mq.getTopic().getName())
            .maxNum(request.getBatchSize())
            .invisibleTime(invisibleTime)
            .pollTime(pollTime)
            .initMode(ConsumeInitMode.MAX)
            .subscriptionData(subscriptionData)
            .fifo(settings.getSubscription().getFifo())
            .attemptId(request.hasAttemptId() ? request.getAttemptId() : null)
            .remainTime(context.getRemainingMs())
            .autoRenew(request.getAutoRenew())
            .filter(new DefaultPopFilter(settings.getBackoffPolicy().getMaxAttempts()))
            .build();
    }

    private SubscriptionData buildFilter(ReceiveMessageRequest request) {
        String topic = request.getMessageQueue().getTopic().getName();
        FilterExpression expression = request.getFilterExpression();

        try {
            String expressionType = GrpcConverter.getInstance()
                .buildExpressionType(expression.getType());

            return FilterAPI.build(
                topic,
                expression.getExpression(),
                expressionType
            );
        } catch (Exception e) {
            log.error("build subscription data error", e);
            return null;
        }
    }

    private Long getPollTime(RequestContext context, ReceiveMessageRequest request, Settings settings) {
        long pollTime;
        long timeRemaining = context.getRemainingMs();

        if (request.hasLongPollingTimeout()) {
            pollTime = Durations.toMillis(request.getLongPollingTimeout());
        } else {
            long requestTimeout = Durations.toMillis(settings.getRequestTimeout());
            pollTime = timeRemaining - requestTimeout / 2;
        }

        if (pollTime < grpcConfig.getMinConsumerPollTime()) {
            pollTime = grpcConfig.getMinConsumerPollTime();
        }

        if (pollTime > grpcConfig.getMaxConsumerPollTime()) {
            pollTime = grpcConfig.getMaxConsumerPollTime();
        }

        if (pollTime > timeRemaining && timeRemaining < grpcConfig.getMinConsumerPollTime()) {
            return null;
        }

        return Math.min(pollTime, timeRemaining);
    }
}
