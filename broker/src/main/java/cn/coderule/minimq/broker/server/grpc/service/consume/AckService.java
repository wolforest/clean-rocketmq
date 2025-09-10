package cn.coderule.minimq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.AckMessageEntry;
import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResponse;
import apache.rocketmq.v2.AckMessageResultEntry;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.server.grpc.converter.AckConverter;
import cn.coderule.minimq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public CompletableFuture<AckMessageResultEntry>[] ackOneByOneAsync(
        RequestContext context, AckMessageRequest request
    ) {
        int size = request.getEntriesCount();
        CompletableFuture<AckMessageResultEntry>[] entryArray = new CompletableFuture[size];

        for (int i = 0; i < size; i++) {
            entryArray[i] = ackOneAsync(context, request, i);
        }

        return entryArray;
    }

    private CompletableFuture<AckMessageResultEntry> ackOneAsync(
        RequestContext context, AckMessageRequest request, int index
    ) {
        CompletableFuture<AckMessageResultEntry> future = new CompletableFuture<>();

        try {
            AckMessageEntry entry = request.getEntries(index);
            AckRequest ackRequest = AckConverter.toAckRequest(context, request, entry);

            consumerController.ack(ackRequest)
                .thenAccept(ackResult -> {
                    future.complete(
                        AckConverter.toResultEntry(context, entry, ackResult)
                    );
                }).exceptionally(t -> {
                    future.complete(
                        AckConverter.toResultEntry(context, entry, t)
                    );
                    return null;
                });
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }


        return future;
    }

    private void ackOneByOneComplete(
        CompletableFuture<AckMessageResponse> result,
        CompletableFuture<AckMessageResultEntry>[] entryArray
    ) {
        Set<Code> responseCodes = new HashSet<>();
        List<AckMessageResultEntry> entryList = new ArrayList<>();

        for (CompletableFuture<AckMessageResultEntry> entryFuture : entryArray) {
            AckMessageResultEntry entry = entryFuture.join();
            responseCodes.add(entry.getStatus().getCode());
            entryList.add(entry);
        }

        AckMessageResponse.Builder responseBuilder = AckMessageResponse
            .newBuilder()
            .addAllEntries(entryList);

        setAckResponseStatus(responseBuilder, responseCodes);
        result.complete(responseBuilder.build());
    }

    private void setAckResponseStatus(AckMessageResponse.Builder response, Set<Code> responseCodes) {
        Status status;
        ResponseBuilder responseBuilder = ResponseBuilder.getInstance();

        if (responseCodes.size() > 1) {
            status = responseBuilder.buildStatus(
                Code.MULTIPLE_RESULTS, Code.MULTIPLE_RESULTS.name()
            );
        } else if (responseCodes.size() == 1) {
            Code code = responseCodes.stream()
                .findAny()
                .get();

            status = responseBuilder.buildStatus(
                code, code.name()
            );
        } else {
            status = responseBuilder.buildStatus(
                Code.INTERNAL_SERVER_ERROR, "ack message result is empty"
            );
        }

        response.setStatus(status);
    }

}
