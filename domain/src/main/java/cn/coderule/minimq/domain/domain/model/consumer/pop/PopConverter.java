package cn.coderule.minimq.domain.domain.model.consumer.pop;

import cn.coderule.minimq.domain.domain.constant.MessageConst;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
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

    public static MessageBO toMessageBO(AckMsg ackMsg, int reviveQid, String reviveTopic, SocketAddress storeHost, long invisibleTime) {
        MessageBO msgInner = new MessageBO();

        msgInner.setTopic(reviveTopic);
        msgInner.setBody(JSON.toJSONString(ackMsg).getBytes(StandardCharsets.UTF_8));
        msgInner.setQueueId(reviveQid);
        msgInner.setTags(PopConstants.ACK_TAG);

        msgInner.setBornTimestamp(System.currentTimeMillis());
        msgInner.setBornHost(storeHost);
        msgInner.setStoreHost(storeHost);
        msgInner.setDeliverTime(ackMsg.getPopTime() + invisibleTime);

        msgInner.getProperties().put(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX, PopKeyBuilder.genAckUniqueId(ackMsg));
        msgInner.setPropertiesString(MessageUtils.propertiesToString(msgInner.getProperties()));
        return msgInner;
    }
}
