package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.broker.domain.consumer.consumer.InflightCounter;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopService {

    private BrokerConfig brokerConfig;
    private InflightCounter inflightCounter;
    private QueueSelector queueSelector;

    private MQFacade mqFacade;
    private PopContextBuilder popContextBuilder;

    private final AtomicLong reviveCount = new AtomicLong(0);

    public CompletableFuture<PopResult> pop(PopRequest request) {
        PopContext context = popContextBuilder.create(request);

        selectQueue(context);
        CompletableFuture<PopResult> result = fetchMessage(context);
        addReceipt(context, result);

        return result;
    }

    private CompletableFuture<PopResult> fetchMessage(PopContext context) {
        CompletableFuture<PopResult> result = PopResult.future();

        if (context.shouldRetry()) {
            result = popRetryMessage(context, result);
        }

        result = popMessage(context, result);

        if (context.shouldRetryAgain()) {
            result = popRetryMessage(context, result);
        }

        return result;
    }

    private CompletableFuture<PopResult> popMessage(PopContext context, CompletableFuture<PopResult> result) {
        Topic topic = context.getTopic();
        int requestQueueId = context.getMessageQueue().getQueueId();

        if (requestQueueId >= 0) {
            return result.thenCompose(popResult -> dequeue(context, topic.getTopicName(), requestQueueId));
        }

        for (int i = 0; i < topic.getReadQueueNums(); i++) {
            int queueId = context.selectRandomQueue(topic.getReadQueueNums(), i);
            result = result.thenCompose(popResult -> dequeue(context, topic.getTopicName(), queueId));
        }

        return result;
    }

    private CompletableFuture<PopResult> popRetryMessage(PopContext context, CompletableFuture<PopResult> result) {
        Topic topic = context.getRetryTopic();

        for (int i = 0; i < topic.getReadQueueNums(); i++) {
            int queueId = context.selectRandomQueue(topic.getReadQueueNums(), i);
            result = result.thenCompose(popResult -> dequeue(context, topic.getTopicName(), queueId));
        }

        return result;
    }

    private CompletableFuture<PopResult> dequeue(PopContext context, String topicName, int queueId) {
        return null;
    }

    private void addReceipt(PopContext context, CompletableFuture<PopResult> result) {
    }

    private void selectQueue(PopContext context) {
        MessageQueue messageQueue = queueSelector.select(context.getRequest());
        context.setMessageQueue(messageQueue);
        selectReviveQueue(context);
    }

    private void selectReviveQueue(PopContext context) {
        if (context.getRequest().isFifo()) {
            context.setReviveQueueId(KeyBuilder.POP_ORDER_REVIVE_QUEUE);
            return;
        }

        int queueNum = brokerConfig.getTopicConfig().getReviveQueueNum();
        int queueId = (int) Math.abs(reviveCount.getAndIncrement() % queueNum);
        context.setReviveQueueId(queueId);
    }
}
