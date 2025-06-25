package cn.coderule.minimq.domain.domain.consumer.pop.helper;

import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.domain.consumer.ack.AckMsg;
import cn.coderule.minimq.domain.domain.consumer.ack.BatchAckMsg;
import cn.coderule.minimq.domain.domain.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.utils.MessageUtils;
import com.alibaba.fastjson2.JSON;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class PopConverter {

    public  static MessageBO toMessageBO(PopCheckPoint popCheckPoint, MessageBO messageExt, SocketAddress storeHost) {
        MessageBO msgInner = new MessageBO();
        initMsgTopic(popCheckPoint, msgInner);
        initMsgTag(messageExt, msgInner);

        msgInner.setBody(messageExt.getBody());
        msgInner.setQueueId(0);
        msgInner.setBornTimestamp(messageExt.getBornTimestamp());
        msgInner.setFlag(messageExt.getFlag());
        msgInner.setSysFlag(messageExt.getSysFlag());
        msgInner.setBornHost(storeHost);
        msgInner.setStoreHost(storeHost);
        msgInner.setReconsumeTimes(messageExt.getReconsumeTimes() + 1);
        msgInner.getProperties().putAll(messageExt.getProperties());

        initMsgProperties(popCheckPoint, messageExt, msgInner);

        return msgInner;
    }

    public static MessageBO toMessage(PopCheckPoint ck, int reviveQid, String reviveTopic, SocketAddress storeHost) {
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

    public static PopCheckPoint toCheckPoint(PopCheckPoint oldCK, long offset) {
        PopCheckPoint newCk = new PopCheckPoint();
        newCk.setBitMap(0);
        newCk.setNum((byte) 1);
        newCk.setPopTime(oldCK.getPopTime());
        newCk.setInvisibleTime(oldCK.getInvisibleTime());
        newCk.setStartOffset(offset);
        newCk.setCId(oldCK.getCId());
        newCk.setTopic(oldCK.getTopic());
        newCk.setQueueId(oldCK.getQueueId());
        newCk.setBrokerName(oldCK.getBrokerName());
        newCk.addDiff(0);

        return newCk;
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

    private static void initMsgTopic(PopCheckPoint popCheckPoint, MessageBO msgInner) {
        if (!popCheckPoint.getTopic().startsWith(MQConstants.RETRY_GROUP_TOPIC_PREFIX)) {
            msgInner.setTopic(KeyBuilder.buildPopRetryTopic(popCheckPoint.getTopic(), popCheckPoint.getCId(), false));
        } else {
            msgInner.setTopic(popCheckPoint.getTopic());
        }
    }

    private static void initMsgTag(MessageBO messageExt, MessageBO msgInner) {
        if (messageExt.getTags() != null) {
            msgInner.setTags(messageExt.getTags());
        } else {
            msgInner.setProperties(new HashMap<>());
        }
    }

    private static void initMsgProperties(PopCheckPoint popCheckPoint, MessageBO messageExt, MessageBO msgInner) {
        if (messageExt.getReconsumeTimes() == 0 || msgInner.getProperties().get(MessageConst.PROPERTY_FIRST_POP_TIME) == null) {
            msgInner.getProperties().put(MessageConst.PROPERTY_FIRST_POP_TIME, String.valueOf(popCheckPoint.getPopTime()));
        }
        msgInner.setPropertiesString(MessageUtils.propertiesToString(msgInner.getProperties()));
    }

}
