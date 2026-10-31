package com.wolfmq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.AckMessageEntry;
import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResponse;
import apache.rocketmq.v2.AckMessageResultEntry;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Status;
import com.wolfmq.broker.api.ConsumerController;
import com.wolfmq.broker.server.grpc.converter.AckConverter;
import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.consumer.ack.broker.AckRequest;
import com.wolfmq.rpc.common.grpc.response.ResponseBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcAckService {
    private final MessageConfig messageConfig;
    private final ConsumerController consumerController;

    public GrpcAckService(
        BrokerConfig brokerConfig,
        ConsumerController consumerController
    ) {
        this.messageConfig = brokerConfig.getMessage();
        this.consumerController = consumerController;
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
                        AckConverter.toResultEntry(entry, ackResult)
                    );
                }).exceptionally(t -> {
                    future.complete(
                        AckConverter.toResultEntry(entry, t)
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
