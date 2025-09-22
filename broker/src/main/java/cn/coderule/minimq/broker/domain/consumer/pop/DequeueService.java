package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DequeueService {
    private final MQFacade mqStore;

    public DequeueService(MQFacade mqStore) {
        this.mqStore = mqStore;
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
            .requestContext(context)
            .consumerGroup(group)
            .topicName(topic)
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

    private boolean shouldStop(PopContext context, String topicName, int queueId, PopResult lastResult) {
        PopRequest request = context.getRequest();
        return lastResult.countMessage() >= request.getMaxNum();
    }

}
