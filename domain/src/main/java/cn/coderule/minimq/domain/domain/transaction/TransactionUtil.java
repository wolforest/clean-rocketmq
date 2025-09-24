package cn.coderule.minimq.domain.domain.transaction;

import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.domain.meta.topic.TopicValidator;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TransactionUtil {
    public static final String REMOVE_TAG = "d";
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final String OFFSET_SEPARATOR = ",";
    public static final String TRANSACTION_ID = "__transactionId__";

    public static String buildOffsetKey(long offset) {
        return offset + OFFSET_SEPARATOR;
    }

    public static String buildOperationTopic() {
        return TopicValidator.RMQ_SYS_TRANS_OP_HALF_TOPIC;
    }

    public static String buildPrepareTopic() {
        return TopicValidator.RMQ_SYS_TRANS_HALF_TOPIC;
    }

    public static String buildDiscardTopic() {
        return TopicValidator.RMQ_SYS_TRANS_CHECK_MAX_TIME_TOPIC;
    }

    public static String buildConsumerGroup() {
        return MQConstants.CID_SYS_RMQ_TRANS;
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
