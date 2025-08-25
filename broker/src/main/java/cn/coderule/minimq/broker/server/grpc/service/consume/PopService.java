package cn.coderule.minimq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.FilterExpression;
import apache.rocketmq.v2.MessageQueue;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.Settings;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.server.grpc.converter.GrpcConverter;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.domain.config.network.GrpcConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.enums.consume.ConsumeStrategy;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.rpc.broker.core.FilterAPI;
import com.google.protobuf.util.Durations;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopService {
    private final BrokerConfig brokerConfig;
    private final GrpcConfig grpcConfig;
    private final ConsumerController consumerController;
    private final SettingManager settingManager;

    public PopService(
        BrokerConfig brokerConfig,
        ConsumerController consumerController,
        SettingManager settingManager
    ) {
        this.brokerConfig = brokerConfig;
        this.grpcConfig = brokerConfig.getGrpcConfig();
        this.consumerController = consumerController;
        this.settingManager = settingManager;
    }

    public void receive(
        RequestContext context,
        ReceiveMessageRequest request,
        StreamObserver<ReceiveMessageResponse> responseObserver
    ) {
        ConsumeResponse response = new ConsumeResponse(consumerController, responseObserver);
        Settings settings = settingManager.getSettings(context);
        if (settings == null) {
            response.noSettings();
            return;
        }

        Long pollTime = getPollTime(context, request, settings);
        if (pollTime == null) {
            response.notEnoughTime();
            return;
        }

        SubscriptionData subscriptionData = buildSubscriptionData(request);
        if (subscriptionData == null) {
            response.illegalFilter();
            return;
        }

        PopRequest popRequest = buildPopRequest(context, request, settings, pollTime, subscriptionData);
        consumerController.popMessage(popRequest)
            .thenAccept(result -> response.writeResponse(context, result));
    }

    private void receiveMessage(PopResult result, PopRequest request, ConsumeResponse response) {
        RequestContext context = request.getRequestContext();

        if (!brokerConfig.getMessageConfig().isEnableAutoRenew() || !request.isAutoRenew()) {
            response.writeResponse(context, result);
            return;
        }

        if (!result.hasFound()) {
            response.writeResponse(context, result);
            return;
        }

        response.writeResponse(context, result);
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
            .consumeStrategy(ConsumeStrategy.CONSUME_FROM_LATEST)
            .subscriptionData(subscriptionData)
            .fifo(settings.getSubscription().getFifo())
            .attemptId(request.hasAttemptId() ? request.getAttemptId() : null)
            .remainTime(context.getRemainingMs())
            .autoRenew(request.getAutoRenew())
            .filter(new DefaultPopFilter(settings.getBackoffPolicy().getMaxAttempts()))
            .build();
    }

    private SubscriptionData buildSubscriptionData(ReceiveMessageRequest request) {
        String topic = request.getMessageQueue().getTopic().getName();
        FilterExpression filter = request.getFilterExpression();

        try {
            String expressionType = GrpcConverter.getInstance()
                .buildExpressionType(filter.getType());
            String expression = StringUtil.notBlank(filter.getExpression())
                ? filter.getExpression()
                : "*";

            return FilterAPI.build(
                topic,
                expression,
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
