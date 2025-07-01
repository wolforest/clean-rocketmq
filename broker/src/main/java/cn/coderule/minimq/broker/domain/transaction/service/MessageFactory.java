package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.domain.transaction.TransactionUtil;
import cn.coderule.minimq.broker.domain.transaction.model.DeleteBuffer;
import cn.coderule.minimq.broker.domain.transaction.model.OffsetQueue;
import cn.coderule.minimq.domain.config.TransactionConfig;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.utils.MessageUtils;

public class MessageFactory {
    private final TransactionConfig transactionConfig;
    private final DeleteBuffer deleteBuffer;

    public MessageFactory(TransactionConfig transactionConfig, DeleteBuffer deleteBuffer) {
        this.transactionConfig = transactionConfig;
        this.deleteBuffer = deleteBuffer;
    }


    private MessageBO createPrepareMessage(MessageBO msg) {
        String uniqId = msg.getMessageId();
        if (uniqId != null && !uniqId.isEmpty()) {
            msg.putProperty(TransactionUtil.TRANSACTION_ID, uniqId);
        }

        msg.putProperty(MessageConst.PROPERTY_REAL_TOPIC, msg.getTopic());
        msg.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, String.valueOf(msg.getQueueId()));

        //reset msg transaction type and set topic = TopicValidator.RMQ_SYS_TRANS_HALF_TOPIC
        msg.setSysFlag(MessageSysFlag.resetTransactionValue(msg.getSysFlag(), MessageSysFlag.TRANSACTION_NOT_TYPE));
        msg.setTopic(TransactionUtil.buildPrepareTopic());
        msg.setQueueId(0);
        msg.setPropertiesString(MessageUtils.propertiesToString(msg.getProperties()));
        return msg;
    }

    public MessageBO recreatePrepareMessage(MessageBO prepareMessage) {
        MessageBO newMsg = new MessageBO();
        newMsg.setWaitStore(false);
        newMsg.setMsgId( prepareMessage.getMsgId());
        newMsg.setTopic( prepareMessage.getProperty(MessageConst.PROPERTY_REAL_TOPIC));
        newMsg.setBody( prepareMessage.getBody());
        String realQueueIdStr =  prepareMessage.getProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);
        if (StringUtil.isNumeric(realQueueIdStr)) {
            newMsg.setQueueId(Integer.parseInt(realQueueIdStr));
        }
        newMsg.setFlag( prepareMessage.getFlag());
        newMsg.setTagsCode( prepareMessage.getTagsCode());
        newMsg.setBornTimestamp( prepareMessage.getBornTimestamp());
        newMsg.setBornHost( prepareMessage.getBornHost());
        newMsg.setTransactionId( prepareMessage.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));

        newMsg.setProperties( prepareMessage.getProperties());
        newMsg.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");
        newMsg.deleteProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET);
        newMsg.deleteProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);

        newMsg.setPropertiesString(MessageUtils.propertiesToString(newMsg.getProperties()));

        int sysFlag =  prepareMessage.getSysFlag();
        sysFlag |= MessageSysFlag.TRANSACTION_PREPARED_TYPE;
        newMsg.setSysFlag(sysFlag);

        return newMsg;
    }

    public MessageBO createCommitMessage(int queueId, String offset) {
        String commitTopic = TransactionUtil.buildCommitTopic();
        OffsetQueue offsetQueue = deleteBuffer.getQueue(queueId);

        int offsetLength = null == offset ? 0 : offset.length();
        int bodyLength = calculateBodyLength(offsetLength, offsetQueue);

        return null;
    }

    private int calculateBodyLength(int offsetLength, OffsetQueue offsetQueue) {
        int bodyLength = offsetLength;
        int maxLength = transactionConfig.getMaxCommitMessageLength();

        if (bodyLength >= maxLength) {
            return bodyLength;
        }

        int size = offsetQueue.getTotalSize();
        if (size > maxLength || bodyLength + size > maxLength) {
            bodyLength = maxLength + 100;
        } else {
            bodyLength += size;
        }

        return bodyLength;
    }
}
