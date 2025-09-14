package cn.coderule.minimq.domain.domain.consumer.ack;

import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopKeyBuilder;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.domain.utils.message.ExtraInfoUtils;
import cn.coderule.minimq.domain.utils.message.MessageUtils;
import java.net.SocketAddress;

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

    public static AckResult toAckResult(AckMessage ackMessage, long popTime) {
        return AckResult.builder()
            .popTime(popTime)
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

    public static PopCheckPoint toCheckpoint(AckMessage ackMessage, long now) {
        AckInfo ackInfo = ackMessage.getAckInfo();
        PopCheckPoint checkPoint = PopCheckPoint.builder()
            .bitMap(0)
            .num((byte) 1)
            .popTime(now)
            .invisibleTime(ackMessage.getInvisibleTime())
            .startOffset(ackInfo.getStartOffset())
            .cid(ackInfo.getConsumerGroup())
            .topic(ackInfo.getTopic())
            .queueId(ackInfo.getQueueId())
            .brokerName(ackInfo.getBrokerName())
            .build();

        checkPoint.addDiff(0);
        return checkPoint;
    }

    public static MessageBO toMessage(AckInfo ackInfo, int reviveQueueId, long deliverTime, SocketAddress storeHost) {
        return null;
    }

    public static MessageBO toMessage(AckMessage ackMessage, PopCheckPoint checkPoint, String reviveTopic, SocketAddress storeHost) {
        MessageBO messageBO = MessageBO.builder()
            .topic(reviveTopic)
            .body(JSONUtil.toJSONString(checkPoint).getBytes(MQConstants.MQ_CHARSET))
            .queueId(ackMessage.getReviveQueueId())
            .bornTimestamp(checkPoint.getPopTime())
            .bornHost(storeHost)
            .storeHost(storeHost)
            .build();

        messageBO.setTags(PopConstants.CK_TAG);
        messageBO.setDeliverTime(checkPoint.getReviveTime() - PopConstants.ackTimeInterval);
        messageBO.setUniqueKey(PopKeyBuilder.genCkUniqueId(checkPoint));
        messageBO.initPropertiesString();

        return messageBO;
    }

    public static CheckPointRequest toCheckPointRequest(InvisibleRequest request) {
        return null;
    }

    public static MessageBO toMessage(InvisibleRequest request) {
        return null;
    }
}
