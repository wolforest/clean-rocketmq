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

        return AckMessage.builder()
            .reviveQueueId(ExtraInfoUtils.getReviveQid(extraInfo))
            .invisibleTime(ExtraInfoUtils.getInvisibleTime(extraInfo))
            .build();
    }

    public static CheckPointRequest toCheckPointRequest(InvisibleRequest request) {
        return null;
    }

    public static MessageBO toMessage(InvisibleRequest request) {
        return null;
    }
}
