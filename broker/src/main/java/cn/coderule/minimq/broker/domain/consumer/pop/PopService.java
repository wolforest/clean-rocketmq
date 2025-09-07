package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.core.enums.code.BrokerExceptionCode;
import cn.coderule.minimq.domain.core.exception.BrokerException;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandler;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopService {
    private final QueueSelector queueSelector;
    private final ContextBuilder contextBuilder;
    private final ReceiptHandler receiptHandler;
    private final DequeueService dequeueService;

    public PopService(
        ContextBuilder contextBuilder,
        QueueSelector queueSelector,
        DequeueService dequeueService,
        ReceiptHandler receiptHandler
    ) {
        this.queueSelector = queueSelector;
        this.receiptHandler = receiptHandler;
        this.contextBuilder = contextBuilder;
        this.dequeueService = dequeueService;
    }

    public CompletableFuture<PopResult> pop(PopRequest request) {
        PopContext context = contextBuilder.build(request);

        queueSelector.select(context);
        CompletableFuture<PopResult> result = popMessage(context);

        result.thenAccept(
            popResult -> addReceipt(context, popResult)
        );
        return result;
    }

    private CompletableFuture<PopResult> popMessage(PopContext context) {
        CompletableFuture<PopResult> result = PopResult.future();

        if (context.shouldRetry()) {
            result = popFromRetryQueue(context, result);
        }

        if (context.hasRequestQueueId()) {
            result = popFromRequestQueue(context, result);
        } else {
            result = popFromTopicQueue(context, result);
        }

        if (context.shouldRetryAgain()) {
            result = popFromRetryQueue(context, result);
        }

        return result;
    }

    private CompletableFuture<PopResult> popFromRequestQueue(PopContext context, CompletableFuture<PopResult> result) {
        Topic topic = context.getTopic();
        String topicName = topic.getTopicName();

        int requestQueueId = context.getRequest().getQueueId();
        if (requestQueueId < 0) {
            throw new BrokerException(BrokerExceptionCode.INTERNAL_SERVER_ERROR, "invalid request queueId");
        }

        return result.thenCompose(
            popResult -> dequeueService.dequeue(context, topicName, requestQueueId, popResult)
        );
    }

    private CompletableFuture<PopResult> popFromTopicQueue(PopContext context, CompletableFuture<PopResult> result) {
        Topic topic = context.getTopic();
        String topicName = topic.getTopicName();

        int queueNum = topic.getReadQueueNums();
        for (int i = 0; i < queueNum; i++) {
            int queueId = context.selectRandomQueue(queueNum, i);

            result = result.thenCompose(
                popResult -> dequeueService.dequeue(context, topicName, queueId, popResult)
            );
        }

        return result;
    }

    private CompletableFuture<PopResult> popFromRetryQueue(PopContext context, CompletableFuture<PopResult> result) {
        Topic topic = context.getRetryTopic();
        String topicName = topic.getTopicName();

        int queueNum = topic.getReadQueueNums();
        for (int i = 0; i < queueNum; i++) {
            int queueId = context.selectRandomQueue(queueNum, i);

            result = result.thenCompose(
                popResult -> dequeueService.dequeue(context, topicName, queueId, popResult)
            );
        }

        return result;
    }

    private void addReceipt(PopContext context, PopResult result) {
        if (result.isEmpty()) {
            return;
        }

        for (MessageBO message : result.getMessageList()) {
            String handle = message.getReceipt();
            if (null == handle) {
                continue;
            }

            MessageReceipt receipt = PopConverter.toReceipt(context, message);
            receiptHandler.addReceipt(receipt);
        }
    }

}
