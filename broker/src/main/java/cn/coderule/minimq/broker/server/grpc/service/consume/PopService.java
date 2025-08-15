package cn.coderule.minimq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.Settings;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.domain.config.network.GrpcConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
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
        ConsumeResponse consumeResponse = new ConsumeResponse(consumerController, responseObserver);
        Settings settings = settingManager.getSettings(context);
        if (settings == null) {
            return consumeResponse.noSettings();
        }

        Long pollTime = getPollTime(context, request, settings);
        if (pollTime == null) {
            return consumeResponse.notEnoughTime();
        }


        long invisibleTime = Durations.toMillis(request.getInvisibleDuration());
        PopRequest popRequest = PopRequest.builder()
            .autoRenew(request.getAutoRenew())
            .invisibleTime(invisibleTime)
            .build();

        return null;
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
