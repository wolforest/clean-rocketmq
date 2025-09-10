package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.consumer.ack.AckConverter;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandler;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckService {
    private final BrokerConfig brokerConfig;

    private final MQFacade mqStore;
    private final TopicFacade topicStore;
    private final ReceiptHandler receiptHandler;


    public AckService(BrokerConfig brokerConfig, MQFacade mqStore, TopicFacade topicStore, ReceiptHandler receiptHandler) {
        this.brokerConfig = brokerConfig;

        this.mqStore = mqStore;
        this.topicStore = topicStore;
        this.receiptHandler = receiptHandler;
    }

    public CompletableFuture<AckResult> ack(AckRequest request) {
        validate(request);
        removeReceipt(request);

        AckMessage ackMessage = AckConverter.toAckMessage(request);
        mqStore.ack(ackMessage);


        return null;
    }

    private void validate(AckRequest request) {
        // validate topic
        Topic topic = validateTopic(request);

        // validate queueId
        validateQueueId(request, topic);

        // validate offset
        validateOffset(request);
    }

    private Topic validateTopic(AckRequest request) {
        Topic topic = topicStore.getTopic(request.getTopicName());
        if (topic != null) {
            return topic;
        }

        log.error("Topic not exists: {}", request.getTopicName());
        throw new InvalidRequestException(
            InvalidCode.ILLEGAL_TOPIC, "Topic not exists: " + request.getTopicName());
    }

    private void validateQueueId(AckRequest request, Topic topic) {
        if (topic.existsQueue(request.getQueueId())) {
            return;
        }

        log.error("Topic queueId error: topic={}, queueId={}",
            request.getTopicName(), request.getQueueId());

        throw new InvalidRequestException(
            InvalidCode.ILLEGAL_TOPIC,
            "Message queue can not find: " + request.getTopicName() + ":" + request.getQueueId()
        );
    }

    private void validateOffset(AckRequest request) {
        QueueRequest queueRequest = QueueRequest.builder()
            .context(request.getRequestContext())
            .topic(request.getTopicName())
            .group(request.getGroupName())
            .queueId(request.getQueueId())
            .build();

        QueueResult minResult = mqStore.getMinOffset(queueRequest);
        QueueResult maxResult = mqStore.getMaxOffset(queueRequest);

        if (request.getOffset() >= minResult.getMinOffset()
            && request.getOffset() <= maxResult.getMaxOffset()) {
            return;
        }

        log.error("invalid offset error: topic={}, queueId={}, offset={}",
            request.getTopicName(), request.getQueueId(), request.getOffset());

        throw new InvalidRequestException(
            InvalidCode.ILLEGAL_OFFSET,
            "Invalid offset: " + request.getTopicName() + ":" + request.getQueueId() + ":" + request.getOffset()
        );
    }

    private void removeReceipt(AckRequest request) {
    }
}
