package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.AckConverter;
import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
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

    private final ConsumerRegister consumerRegister;

    public AckService(
        BrokerConfig brokerConfig,
        MQFacade mqStore,
        TopicFacade topicStore,
        ConsumerRegister consumerRegister,
        ReceiptHandler receiptHandler
    ) {
        this.brokerConfig = brokerConfig;

        this.mqStore = mqStore;
        this.topicStore = topicStore;
        this.consumerRegister = consumerRegister;
        this.receiptHandler = receiptHandler;
    }

    public CompletableFuture<AckResult> ack(AckRequest request) {
        removeReceipt(request);

        AckMessage ackMessage = AckConverter.toAckMessage(request);
        validate(ackMessage);

        mqStore.ack(ackMessage);

        AckResult result = AckResult.success();
        return CompletableFuture.completedFuture(result);
    }

    private void validate(AckMessage ackMessage) {
        // validate receipt handle
        if (ackMessage.getReceiptHandle().isExpired()) {
            throw new InvalidRequestException(
                InvalidCode.INVALID_RECEIPT_HANDLE,
                "Receipt handle is expired: " + ackMessage.getReceiptStr()
            );
        }

        // validate topic
        Topic topic = validateTopic(ackMessage);

        // validate queueId
        validateQueueId(ackMessage, topic);

        // validate offset
        validateOffset(ackMessage);
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

    private void removeReceipt(AckRequest request) {
        MessageReceipt requestReceipt =  buildRequestReceipt(request);
        if (requestReceipt == null) {
            return;
        }

        MessageReceipt receipt = receiptHandler.removeReceipt(requestReceipt);
        if (receipt == null) {
            return;
        }

        request.setReceiptStr(receipt.getReceiptHandleStr());
    }

    private MessageReceipt buildRequestReceipt(AckRequest request) {
        ClientChannelInfo channelInfo = consumerRegister.findChannel(
            request.getGroupName(),
            request.getRequestContext().getClientID()
        );
        if (channelInfo == null) {
            return null;
        }

        return MessageReceipt.builder()
            .group(request.getGroupName())
            .messageId(request.getMessageId())
            .receiptHandleStr(request.getReceiptStr())
            .channel(channelInfo.getChannel())
            .build();
    }
}
