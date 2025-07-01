package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.domain.transaction.TransactionUtil;
import cn.coderule.minimq.broker.domain.transaction.model.DeleteBuffer;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.utils.MessageUtils;

public class MessageFactory {
    private final DeleteBuffer deleteBuffer;

    public MessageFactory(DeleteBuffer deleteBuffer) {
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

    public MessageBO recreatePrepareMessage(MessageBO message) {
        MessageBO prepareMsg = new MessageBO();
        prepareMsg.setWaitStore(false);
        prepareMsg.setMsgId(message.getMsgId());
        prepareMsg.setTopic(message.getProperty(MessageConst.PROPERTY_REAL_TOPIC));
        prepareMsg.setBody(message.getBody());
        String realQueueIdStr = message.getProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);
        if (StringUtil.isNumeric(realQueueIdStr)) {
            prepareMsg.setQueueId(Integer.parseInt(realQueueIdStr));
        }
        prepareMsg.setFlag(message.getFlag());
        prepareMsg.setTagsCode(message.getTagsCode());
        prepareMsg.setBornTimestamp(message.getBornTimestamp());
        prepareMsg.setBornHost(message.getBornHost());
        prepareMsg.setTransactionId(message.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));

        prepareMsg.setProperties(message.getProperties());
        prepareMsg.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");
        prepareMsg.deleteProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET);
        prepareMsg.deleteProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);

        prepareMsg.setPropertiesString(MessageUtils.propertiesToString(prepareMsg.getProperties()));

        int sysFlag = message.getSysFlag();
        sysFlag |= MessageSysFlag.TRANSACTION_PREPARED_TYPE;
        prepareMsg.setSysFlag(sysFlag);

        return prepareMsg;
    }

    public MessageBO createCommitMessage(int queueId, String offset) {
        return null;
    }
}
