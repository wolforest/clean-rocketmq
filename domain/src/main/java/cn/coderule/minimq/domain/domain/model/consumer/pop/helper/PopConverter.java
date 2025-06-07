package cn.coderule.minimq.domain.domain.model.consumer.pop.helper;

import cn.coderule.minimq.domain.domain.constant.MessageConst;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.model.consumer.pop.ack.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.ack.BatchAckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.utils.MessageUtils;
import com.alibaba.fastjson2.JSON;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class PopConverter {
    public static MessageBO buildCkMsg(PopCheckPoint ck, int reviveQid, String reviveTopic, SocketAddress storeHost) {
        MessageBO msgInner = new MessageBO();

        msgInner.setTopic(reviveTopic);
        msgInner.setBody(JSON.toJSONString(ck).getBytes(StandardCharsets.UTF_8));
        msgInner.setQueueId(reviveQid);
        msgInner.setTags(PopConstants.CK_TAG);
        msgInner.setBornTimestamp(System.currentTimeMillis());
        msgInner.setBornHost(storeHost);
        msgInner.setStoreHost(storeHost);
        msgInner.setDeliverTime(ck.getReviveTime() - PopConstants.ackTimeInterval);
        msgInner.getProperties().put(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX, PopKeyBuilder.genCkUniqueId(ck));
        msgInner.setPropertiesString(MessageUtils.propertiesToString(msgInner.getProperties()));

        return msgInner;
    }

    public static PopCheckPoint toCheckPoint(AckMsg ackMsg, long offset) {
        PopCheckPoint point = new PopCheckPoint();
        point.setStartOffset(ackMsg.getStartOffset());
        point.setPopTime(ackMsg.getPopTime());
        point.setQueueId(ackMsg.getQueueId());
        point.setCId(ackMsg.getConsumerGroup());
        point.setTopic(ackMsg.getTopic());
        point.setNum((byte) 0);
        point.setBitMap(0);
        point.setReviveOffset(offset);
        point.setBrokerName(ackMsg.getBrokerName());
        return point;
    }

    public static MessageBO toMessageBO(AckMsg ackMsg, int reviveQid, String reviveTopic, SocketAddress storeHost, long invisibleTime) {
        MessageBO msgInner = new MessageBO();

        msgInner.setTopic(reviveTopic);
        msgInner.setBody(JSON.toJSONString(ackMsg).getBytes(StandardCharsets.UTF_8));
        msgInner.setQueueId(reviveQid);

        if (ackMsg instanceof BatchAckMsg) {
            msgInner.setTags(PopConstants.BATCH_ACK_TAG);
            msgInner.setMessageId(PopKeyBuilder.genBatchAckUniqueId((BatchAckMsg) ackMsg));
        } else {
            msgInner.setTags(PopConstants.ACK_TAG);
            msgInner.setMessageId(PopKeyBuilder.genAckUniqueId(ackMsg));
        }

        msgInner.setBornTimestamp(System.currentTimeMillis());
        msgInner.setBornHost(storeHost);
        msgInner.setStoreHost(storeHost);
        msgInner.setDeliverTime(ackMsg.getPopTime() + invisibleTime);

        msgInner.setPropertiesString(MessageUtils.propertiesToString(msgInner.getProperties()));
        return msgInner;
    }
}
