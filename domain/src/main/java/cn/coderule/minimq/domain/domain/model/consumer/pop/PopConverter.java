package cn.coderule.minimq.domain.domain.model.consumer.pop;

import cn.coderule.minimq.domain.domain.constant.MessageConst;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.utils.MessageUtils;
import com.alibaba.fastjson2.JSON;
import java.net.SocketAddress;

public class PopConverter {
    public static MessageBO buildCkMsg(PopCheckPoint ck, int reviveQid, String reviveTopic, SocketAddress storeHost) {
        MessageBO msgInner = new MessageBO();

        msgInner.setTopic(reviveTopic);
        msgInner.setBody(JSON.toJSONString(ck).getBytes());
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
}
