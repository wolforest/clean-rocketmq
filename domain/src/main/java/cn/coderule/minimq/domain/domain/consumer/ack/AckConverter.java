package cn.coderule.minimq.domain.domain.consumer.ack;

import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.message.MessageBO;

public class AckConverter {
    public static AckMessage toAckMessage(AckRequest request) {


        return null;
    }

    public static CheckPointRequest toCheckPointRequest(InvisibleRequest request) {
        return null;
    }

    public static MessageBO toMessage(InvisibleRequest request) {
        return null;
    }
}
