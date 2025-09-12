package cn.coderule.minimq.domain.domain.consumer.ack;

import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.utils.message.ExtraInfoUtils;

public class AckConverter {
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
            .reviveQueueId(ExtraInfoUtils.getReviveQid(extraInfo))
            .invisibleTime(ExtraInfoUtils.getInvisibleTime(extraInfo))
            .ackInfo(ackInfo)
            .build();
    }

    public static CheckPointRequest toCheckPointRequest(InvisibleRequest request) {
        return null;
    }

    public static MessageBO toMessage(InvisibleRequest request) {
        return null;
    }
}
