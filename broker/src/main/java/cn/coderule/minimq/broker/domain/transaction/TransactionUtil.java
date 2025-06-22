package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.domain.constant.MQConstants;
import cn.coderule.minimq.domain.domain.constant.MessageConst;
import cn.coderule.minimq.domain.domain.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.meta.topic.TopicValidator;
import cn.coderule.minimq.domain.utils.MessageUtils;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TransactionUtil {
    public static final String REMOVE_TAG = "d";
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final String OFFSET_SEPARATOR = ",";
    public static final String TRANSACTION_ID = "__transactionId__";

    public static String buildOpTopic() {
        return TopicValidator.RMQ_SYS_TRANS_OP_HALF_TOPIC;
    }

    public static String buildHalfTopic() {
        return TopicValidator.RMQ_SYS_TRANS_HALF_TOPIC;
    }

    public static String buildConsumerGroup() {
        return MQConstants.CID_SYS_RMQ_TRANS;
    }

    public static MessageBO buildPrepareMessage(MessageBO message) {
        final MessageBO prepareMsg = new MessageBO();
        prepareMsg.setWaitStore(false);
        prepareMsg.setMsgId(message.getMsgId());
        prepareMsg.setTopic(message.getProperty(MessageConst.PROPERTY_REAL_TOPIC));
        prepareMsg.setBody(message.getBody());
        final String realQueueIdStr = message.getProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);
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

    public static long getImmunityTime(String checkImmunityTimeStr, long transactionTimeout) {
        long checkImmunityTime = 0;

        try {
            checkImmunityTime = Long.parseLong(checkImmunityTimeStr) * 1000;
        } catch (Throwable ignored) {
        }

        //If a custom first check time is set, the minimum check time;
        //The default check protection period is transactionTimeout
        if (checkImmunityTime < transactionTimeout) {
            checkImmunityTime = transactionTimeout;
        }
        return checkImmunityTime;
    }
}
