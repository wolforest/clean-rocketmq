package cn.coderule.minimq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResponse;
import apache.rocketmq.v2.AckMessageResultEntry;
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
            // default value of enableBatchAck is false
            if (messageConfig.isEnableBatchAck()) {
                return ackBatch(context, request);
            } else {
                return ackOneByOne(context, request);
            }
        } catch (Throwable t) {
            return ackException(context, t);
        }
    }

    public CompletableFuture<AckMessageResponse> ackBatch(RequestContext context, AckMessageRequest request) {
        return null;
    }

    public CompletableFuture<AckMessageResponse> ackOneByOne(RequestContext context, AckMessageRequest request) {
        CompletableFuture<AckMessageResponse> result = new CompletableFuture<>();
        CompletableFuture<AckMessageResultEntry>[] entryArray = ackOneByOneAsync(context, request);

        CompletableFuture.allOf(entryArray).whenComplete((ackResult, throwable) -> {
            if (null != throwable) {
                result.completeExceptionally(throwable);
                return;
            }

            ackOneByOneComplete(result, entryArray);
        });

        return result;
    }

    public CompletableFuture<AckMessageResponse> ackException(RequestContext context, Throwable t) {
        CompletableFuture<AckMessageResponse> future = new CompletableFuture<>();
        future.completeExceptionally(t);

        return future;
    }

    @SuppressWarnings("unchecked")
    public CompletableFuture<AckMessageResultEntry>[] ackOneByOneAsync(RequestContext context, AckMessageRequest request) {
        int size = request.getEntriesCount();
        CompletableFuture<AckMessageResultEntry>[] entryArray = new CompletableFuture[size];

        for (int i = 0; i < size; i++) {
            entryArray[i] = ackOneAsync(context, request, i);
        }

        return entryArray;
    }

    private void ackOneByOneComplete(
        CompletableFuture<AckMessageResponse> result,
        CompletableFuture<AckMessageResultEntry>[] entryArray
    ) {

    }

    private CompletableFuture<AckMessageResultEntry> ackOneAsync(
        RequestContext context,
        AckMessageRequest request,
        int index
    ) {
        return null;
    }

}
