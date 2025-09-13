package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AckValidator {
    private final MQFacade mqStore;
    private final TopicFacade topicStore;

    public AckValidator(MQFacade mqStore, TopicFacade topicStore) {
        this.mqStore = mqStore;
        this.topicStore = topicStore;
    }

    public void validate(AckMessage ackMessage) {
        // validate receipt handle
        validateReceipt(ackMessage);

        // validate topic
        Topic topic = validateTopic(ackMessage);

        // validate queueId
        validateQueueId(ackMessage, topic);

        // validate offset
        validateOffset(ackMessage);
    }

    private void validateReceipt(AckMessage ackMessage) {
        if (!ackMessage.getReceiptHandle().isExpired()) {
            return;
        }

        throw new InvalidRequestException(
            InvalidCode.INVALID_RECEIPT_HANDLE,
            "Receipt handle is expired: " + ackMessage.getReceiptStr()
        );
    }

    private Topic validateTopic(AckMessage ackMessage) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        Topic topic = topicStore.getTopic(ackInfo.getTopic());
        if (topic != null) {
            return topic;
        }

        log.error("Topic not exists: {}", ackInfo.getTopic());
        throw new InvalidRequestException(
            InvalidCode.ILLEGAL_TOPIC, "Topic not exists: " + ackInfo.getTopic());
    }

    private void validateQueueId(AckMessage ackMessage, Topic topic) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        if (topic.existsQueue(ackInfo.getQueueId())) {
            return;
        }

        log.error("Topic queueId error: topic={}, queueId={}",
            ackInfo.getTopic(), ackInfo.getQueueId());

        throw new InvalidRequestException(
            InvalidCode.ILLEGAL_TOPIC,
            "Message queue can not find: " + ackInfo.getTopic() + ":" + ackInfo.getQueueId()
        );
    }

    private void validateOffset(AckMessage ackMessage) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        QueueRequest queueRequest = QueueRequest.builder()
            .context(ackMessage.getRequestContext())
            .topic(ackInfo.getTopic())
            .group(ackInfo.getConsumerGroup())
            .queueId(ackInfo.getQueueId())
            .build();

        QueueResult minResult = mqStore.getMinOffset(queueRequest);
        QueueResult maxResult = mqStore.getMaxOffset(queueRequest);

        if (ackInfo.getAckOffset() >= minResult.getMinOffset()
            && ackInfo.getAckOffset() <= maxResult.getMaxOffset()) {
            return;
        }

        log.error("invalid offset error: topic={}, queueId={}, offset={}",
            ackInfo.getTopic(), ackInfo.getQueueId(), ackInfo.getAckOffset());

        throw new InvalidRequestException(
            InvalidCode.ILLEGAL_OFFSET,
            "Invalid offset: " + ackInfo.getTopic() + ":" + ackInfo.getQueueId() + ":" + ackInfo.getAckOffset()
        );
    }

}
