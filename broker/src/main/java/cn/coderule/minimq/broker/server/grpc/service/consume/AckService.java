package cn.coderule.minimq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResponse;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckService {
    private final MessageConfig messageConfig;
    private final ConsumerController consumerController;
    private final SettingManager settingManager;
    private final ChannelManager channelManager;

    public AckService(
        BrokerConfig brokerConfig,
        ConsumerController consumerController,
        SettingManager settingManager,
        ChannelManager channelManager
    ) {
        this.messageConfig = brokerConfig.getMessageConfig();
        this.consumerController = consumerController;
        this.settingManager = settingManager;
        this.channelManager = channelManager;
    }

    public CompletableFuture<AckMessageResponse> ack(RequestContext context, AckMessageRequest request) {
        try {
            if (messageConfig.isEnableBatchAck()) {
                return batchAck(context, request);
            } else {
                return ack(context, request);
            }
        } catch (Throwable t) {
            return ackException(context, t);
        }
    }

    public CompletableFuture<AckMessageResponse> batchAck(RequestContext context, AckMessageRequest request) {
        return null;
    }

    public CompletableFuture<AckMessageResponse> ackOneByOne(RequestContext context, AckMessageRequest request) {
        return null;
    }

    public CompletableFuture<AckMessageResponse> ackException(RequestContext context, Throwable t) {
        CompletableFuture<AckMessageResponse> future = new CompletableFuture<>();
        future.completeExceptionally(t);

        return future;
    }

}
