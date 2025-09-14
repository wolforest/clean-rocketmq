package cn.coderule.minimq.domain.domain.consumer.consume.pop.helper;

import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.core.enums.consume.PopStatus;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.BatchAckInfo;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopContext;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.order.OrderRequest;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.utils.message.MessageUtils;
import com.alibaba.fastjson2.JSON;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public static PopCheckPoint toCheckPoint(DequeueRequest request, DequeueResult result) {
        long firstOffset = result.getFirstMessage().getQueueOffset();

        PopCheckPoint checkPoint = PopCheckPoint.builder()
            .bitMap(0)
            .num((byte) result.countMessage())
            .popTime(request.getDequeueTime())
            .invisibleTime(request.getInvisibleTime())
            .startOffset(firstOffset)
            .topic(request.getTopic())
            .cid(request.getGroup())
            .queueId(request.getQueueId())
            .build();

        for (Long offset : result.getOffsetList()) {
            checkPoint.addDiff((int)(offset - firstOffset));
        }

        return checkPoint;
    }

    public static PopCheckPoint toCheckPoint(AckInfo ackInfo, long offset) {
        PopCheckPoint point = new PopCheckPoint();
        point.setStartOffset(ackInfo.getStartOffset());
        point.setPopTime(ackInfo.getPopTime());
        point.setQueueId(ackInfo.getQueueId());
        point.setCId(ackInfo.getConsumerGroup());
        point.setTopic(ackInfo.getTopic());
        point.setNum((byte) 0);
        point.setBitMap(0);
        point.setReviveOffset(offset);
        point.setBrokerName(ackInfo.getBrokerName());
        return point;
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

    public static MessageReceipt toReceipt(PopContext context, MessageBO messageBO) {
        String receiptStr = messageBO.getReceipt();
        ReceiptHandle receiptHandle = ReceiptHandle.decode(receiptStr);

        PopRequest request = context.getRequest();
        return MessageReceipt.builder()
            .requestContext(request.getRequestContext())
            .channel(context.getChannel())
            .group(request.getConsumerGroup())
            .topic(request.getTopicName())
            .queueId(request.getQueueId())
            .receiptHandleStr(receiptStr)
            .originalReceiptHandleStr(receiptStr)
            .messageId(messageBO.getUniqueKey())
            .queueOffset(messageBO.getQueueOffset())
            .reconsumeTimes(messageBO.getReconsumeTimes())
            .originalReceiptHandle(receiptHandle)
            .consumeTimestamp(receiptHandle.getRetrieveTime())
            .build();
    }

    public static PopResult toPopResult(PopContext context, DequeueResult dequeueResult,  PopResult lastResult) {
        if (dequeueResult.isEmpty()) {
            return lastResult;
        }

        List<MessageBO> messageList = new ArrayList<>();
        messageList.addAll(lastResult.getMessageList());
        messageList.addAll(dequeueResult.getMessageList());

        return PopResult.builder()
            .restNum(0)
            .nextOffset(dequeueResult.getNextOffset())
            .popStatus(toPopStatus(dequeueResult.getStatus(), lastResult.getPopStatus()))
            .messageList(messageList)
            .popTime(context.getPopTime())
            .invisibleTime(context.getRequest().getInvisibleTime())
            .build();
    }

    public static PopStatus toPopStatus(MessageStatus status, PopStatus lastStatus) {
        PopStatus newStatus = switch (status) {
            case FOUND:
                yield PopStatus.FOUND;
            case OFFSET_TOO_SMALL:
            case OFFSET_OVERFLOW_ONE:
            case OFFSET_OVERFLOW_BADLY:
                yield PopStatus.NO_NEW_MSG;
            case OFFSET_FOUND_NULL:
            case NO_MATCHED_MESSAGE:
            case OFFSET_RESET:
            case MESSAGE_WAS_REMOVING:
            default:
                yield PopStatus.POLLING_NOT_FOUND;
        };

        if (newStatus != PopStatus.FOUND) {
            return lastStatus;
        }

        return newStatus;
    }

    public static OrderRequest toOrderRequest(DequeueRequest request, DequeueResult result) {
        return OrderRequest.builder()
            .requestContext(request.getRequestContext())
            .storeGroup(request.getStoreGroup())
            .attemptId(request.getAttemptId())
            .topicName(request.getTopic())
            .consumerGroup(request.getGroup())
            .queueId(request.getQueueId())
            .dequeueTime(request.getDequeueTime())
            .invisibleTime(request.getInvisibleTime())
            .queueOffset(request.getOffset())
            .offsetList(result.getOffsetList())
            .build();
    }

}
