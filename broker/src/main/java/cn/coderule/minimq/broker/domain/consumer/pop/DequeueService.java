package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.broker.domain.consumer.consumer.InflightCounter;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.rpc.store.facade.ConsumeOrderFacade;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DequeueService {
    private final BrokerConfig brokerConfig;

    private final MQFacade mqStore;
    private final ConsumeOrderFacade orderStore;
    private final InflightCounter inflightCounter;

    public DequeueService(
        BrokerConfig brokerConfig,
        InflightCounter inflightCounter,
        MQFacade mqStore,
        ConsumeOrderFacade orderStore
    ) {
        this.brokerConfig = brokerConfig;
        this.inflightCounter = inflightCounter;
        this.mqStore = mqStore;
        this.orderStore = orderStore;
    }

    public CompletableFuture<PopResult> dequeue(
        PopContext context,
        String topicName,
        int queueId,
        PopResult lastResult
    ) {
        if (shouldStop(context, topicName, queueId, lastResult)) {
            return stopDequeue(context, topicName, queueId, lastResult);
        }

        if (!validateOrderMessage(context, topicName, queueId)) {
            return stopDequeue(context, topicName, queueId, lastResult);
        }

        // @question need to add an atomic long to store restNum?
        DequeueRequest request = buildDequeueRequest(context, topicName, queueId);
        request.setMaxNum(calculateMaxNum(context, lastResult));

        return mqStore.dequeueAsync(request)
            .thenApply(
                result -> processResult(context, result, topicName, queueId, lastResult)
            );
    }

    private int calculateMaxNum(PopContext context, PopResult lastResult) {
        int maxNum = context.getRequest().getMaxNum();

        return maxNum - lastResult.countMessage();
    }

    private PopResult processResult(
        PopContext context,
        DequeueResult dequeueResult,
        String topicName,
        int queueId,
        PopResult lastResult
    ) {
        PopResult newResult = PopConverter.toPopResult(context, dequeueResult, lastResult);

        long restNum = calculateNextNum(context, topicName, queueId, lastResult);
        newResult.setRestNum(restNum);

        String consumerGroup = context.getRequest().getConsumerGroup();
        inflightCounter.increment(topicName, consumerGroup, queueId, dequeueResult.countMessage());

        return newResult;
    }

    private long calculateNextNum(PopContext context, String topic, int queueId, PopResult lastResult) {
        long maxOffset = getMaxOffset(context, topic, queueId);
        long restNum = lastResult.getRestNum();
        if (maxOffset > lastResult.getNextOffset()) {
            restNum += maxOffset - lastResult.getNextOffset();
        }

        return restNum;
    }

    private long getMaxOffset(PopContext context, String topic, int queueId) {
        PopRequest request = context.getRequest();
        return getMaxOffset(request.getRequestContext(), request.getConsumerGroup(), topic, queueId);
    }

    private long getMaxOffset(RequestContext context, String group, String topic, int queueId) {
        QueueRequest request = QueueRequest.builder()
            .context(context)
            .group(group)
            .topic(topic)
            .queueId(queueId)
            .build();

        return mqStore.getMaxOffset(request).getMaxOffset();
    }

    private DequeueRequest buildDequeueRequest(PopContext context, String topicName, int queueId) {
        PopRequest request = context.getRequest();
        return DequeueRequest.builder()
            .requestContext(request.getRequestContext())
            .attemptId(request.getAttemptId())
            .group(request.getConsumerGroup())
            .topic(topicName)
            .queueId(queueId)
            .reviveQueueId(context.getReviveQueueId())
            .num(request.getMaxNum())
            .maxNum(request.getMaxNum())
            .fifo(request.isFifo())
            .dequeueTime(context.getPopTime())
            .invisibleTime(request.getInvisibleTime())
            .consumeStrategy(request.getConsumeStrategy())
            .build();
    }

    private CompletableFuture<PopResult> stopDequeue(PopContext context, String topicName, int queueId, PopResult lastResult) {
        CompletableFuture<PopResult> result = new CompletableFuture<>();

        long maxOffset = getMaxOffset(context, topicName, queueId);
        if (maxOffset > lastResult.getNextOffset()) {
            lastResult.increaseRestNum(maxOffset - lastResult.getNextOffset());
        }

        result.complete(lastResult);
        return result;
    }

    private OrderRequest createOrderRequest(PopRequest request, String topicName, int queueId) {
        return OrderRequest.builder()
            .topicName(topicName)
            .consumerGroup(request.getConsumerGroup())
            .queueId(queueId)
            .attemptId(request.getAttemptId())
            .invisibleTime(request.getInvisibleTime())
            .build();
    }

    private boolean validateOrderMessage(PopContext context, String topicName, int queueId) {
        PopRequest request = context.getRequest();
        if (!request.isFifo()) {
            return true;
        }

        OrderRequest orderRequest = createOrderRequest(request, topicName, queueId);
        if (!orderStore.isLocked(orderRequest)) {
            return false;
        }

        inflightCounter.clear(topicName, request.getConsumerGroup(), queueId);
        return true;
    }

    private boolean shouldStop(PopContext context, String topicName, int queueId, PopResult lastResult) {
        PopRequest request = context.getRequest();
        if (lastResult.countMessage() >= request.getMaxNum()) {
            return true;
        }

        MessageConfig messageConfig = brokerConfig.getMessageConfig();
        if (!messageConfig.isEnablePopThreshold()) {
            return false;
        }

        long inflight = inflightCounter.get(topicName, request.getConsumerGroup(), queueId);
        boolean status = inflight > messageConfig.getPopInflightThreshold();
        if (status) {
            log.warn("Stop pop because too much message inflight,"
                    + "topic={}; group={}, queueId={}",
                topicName, request.getConsumerGroup(), queueId
            );
        }

        return status;
    }

}
