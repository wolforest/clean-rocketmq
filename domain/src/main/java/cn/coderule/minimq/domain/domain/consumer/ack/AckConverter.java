package cn.coderule.minimq.domain.domain.consumer.ack;

import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.utils.message.ExtraInfoUtils;

public class AckConverter {
    public static AckMessage toAckMessage(AckRequest request) {
        String[] extraInfo = ExtraInfoUtils.split(request.getExtraInfo());

        AckInfo ackInfo = AckInfo.builder()
            .topic(request.getTopicName())
            .consumerGroup(request.getGroupName())
            .queueId(request.getQueueId())
            .ackOffset(request.getOffset())
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
