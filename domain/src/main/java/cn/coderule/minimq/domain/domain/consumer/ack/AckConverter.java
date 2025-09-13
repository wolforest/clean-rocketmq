package cn.coderule.minimq.domain.domain.consumer.ack;

import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.domain.utils.message.ExtraInfoUtils;

public class AckConverter {

    public static AckMessage toAckMessage(InvisibleRequest request) {
        String[] extraInfo = ExtraInfoUtils.split(request.getReceiptStr());
        ReceiptHandle handle = ReceiptHandle.decode(request.getReceiptStr());

        String realTopic = handle.getRealTopic(request.getTopicName(), request.getGroupName());

        AckInfo ackInfo = AckInfo.builder()
            .topic(realTopic)
            .consumerGroup(request.getGroupName())
            .queueId(handle.getQueueId())
            .ackOffset(handle.getOffset())
            .startOffset(ExtraInfoUtils.getCkQueueOffset(extraInfo))
            .popTime(ExtraInfoUtils.getPopTime(extraInfo))
            .brokerName(ExtraInfoUtils.getBrokerName(extraInfo))
            .build();

        return AckMessage.builder()
            .requestContext(request.getRequestContext())
            .reviveQueueId(ExtraInfoUtils.getReviveQid(extraInfo))
            .invisibleTime(ExtraInfoUtils.getInvisibleTime(extraInfo))
            .ackInfo(ackInfo)
            .receiptHandle(handle)
            .receiptStr(request.getReceiptStr())
            .isOrderly(ExtraInfoUtils.isOrder(extraInfo))
            .commitOffset(handle.getCommitLogOffset())
            .build();
     }

    public static AckMessage toAckMessage(AckRequest request) {
        String[] extraInfo = ExtraInfoUtils.split(request.getReceiptStr());
        ReceiptHandle handle = ReceiptHandle.decode(request.getReceiptStr());

        String realTopic = handle.getRealTopic(request.getTopicName(), request.getGroupName());
        AckInfo ackInfo = AckInfo.builder()
            .topic(realTopic)
            .consumerGroup(request.getGroupName())
            .queueId(handle.getQueueId())
            .ackOffset(handle.getOffset())
            .startOffset(ExtraInfoUtils.getCkQueueOffset(extraInfo))
            .popTime(ExtraInfoUtils.getPopTime(extraInfo))
            .brokerName(ExtraInfoUtils.getBrokerName(extraInfo))
            .build();

        return AckMessage.builder()
            .requestContext(request.getRequestContext())

            .ackInfo(ackInfo)
            .receiptHandle(handle)
            .receiptStr(request.getReceiptStr())
            .reviveQueueId(ExtraInfoUtils.getReviveQid(extraInfo))
            .invisibleTime(ExtraInfoUtils.getInvisibleTime(extraInfo))
            .isOrderly(ExtraInfoUtils.isOrder(extraInfo))
            .commitOffset(handle.getCommitLogOffset())
            .build();
    }

    public static AckResult toAckResult(AckMessage ackMessage) {
        return AckResult.builder()
            .receiptStr(ackMessage.getReceiptStr())
            .reviveQueueId(ackMessage.getReviveQueueId())
            .invisibleTime(ackMessage.getInvisibleTime())
            .commitOffset(ackMessage.getCommitOffset())
            .build();
    }

    public static OrderRequest toOrderRequest(AckMessage ackMessage) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        return OrderRequest.builder()
            .requestContext(ackMessage.getRequestContext())
            .storeGroup(ackMessage.getStoreGroup())

            .topicName(ackInfo.getTopic())
            .consumerGroup(ackInfo.getConsumerGroup())
            .queueId(ackInfo.getQueueId())
            .queueOffset(ackInfo.getAckOffset())
            .dequeueTime(ackInfo.getPopTime())
            .build();
    }

    public static CheckPointRequest toCheckPointRequest(InvisibleRequest request) {
        return null;
    }

    public static MessageBO toMessage(InvisibleRequest request) {
        return null;
    }
}
