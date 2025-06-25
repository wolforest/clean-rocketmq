package cn.coderule.minimq.domain.domain.consumer.pop.helper;

import cn.coderule.minimq.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.domain.consumer.ack.AckMsg;
import cn.coderule.minimq.domain.domain.consumer.ack.BatchAckMsg;
import cn.coderule.minimq.domain.domain.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;

public class PopKeyBuilder {

    public static String buildLockKey(PopCheckPoint point) {
        return KeyBuilder.buildConsumeKey(point.getTopic(), point.getCId(), point.getQueueId());
    }

    public static String buildKey(AckMsg ackMsg) {
        return ackMsg.getTopic()
            + ackMsg.getConsumerGroup()
            + ackMsg.getQueueId()
            + ackMsg.getStartOffset()
            + ackMsg.getPopTime()
            + ackMsg.getBrokerName();
    }

    public static String buildKey(PopCheckPoint point) {
        return point.getTopic()
            + point.getCId()
            + point.getQueueId()
            + point.getStartOffset()
            + point.getPopTime()
            + point.getBrokerName();
    }

    public static String buildReviveKey(AckMsg ackMsg) {
        return ackMsg.getTopic()
            + ackMsg.getConsumerGroup()
            + ackMsg.getQueueId()
            + ackMsg.getStartOffset()
            + ackMsg.getPopTime();
    }

    public static String buildReviveKey(PopCheckPoint point) {
        return point.getTopic()
            + point.getCId()
            + point.getQueueId()
            + point.getStartOffset()
            + point.getPopTime();
    }

    /**
     * moved from org.apache.rocketmq.broker.util.PopUtils
     */
    public static String genAckUniqueId(AckMsg ackMsg) {
        return ackMsg.getTopic()
            + PopConstants.SPLIT + ackMsg.getQueueId()
            + PopConstants.SPLIT + ackMsg.getAckOffset()
            + PopConstants.SPLIT + ackMsg.getConsumerGroup()
            + PopConstants.SPLIT + ackMsg.getPopTime()
            + PopConstants.SPLIT + ackMsg.getBrokerName()
            + PopConstants.SPLIT + PopConstants.ACK_TAG;
    }

    /**
     * moved from org.apache.rocketmq.broker.util.PopUtils
     */
    public static String genBatchAckUniqueId(BatchAckMsg batchAckMsg) {
        return batchAckMsg.getTopic()
            + PopConstants.SPLIT + batchAckMsg.getQueueId()
            + PopConstants.SPLIT + batchAckMsg.getAckOffsetList().toString()
            + PopConstants.SPLIT + batchAckMsg.getConsumerGroup()
            + PopConstants.SPLIT + batchAckMsg.getPopTime()
            + PopConstants.SPLIT + PopConstants.BATCH_ACK_TAG;
    }

    /**
     * moved from org.apache.rocketmq.broker.util.PopUtils
     */
    public static String genCkUniqueId(PopCheckPoint ck) {
        return ck.getTopic()
            + PopConstants.SPLIT + ck.getQueueId()
            + PopConstants.SPLIT + ck.getStartOffset()
            + PopConstants.SPLIT + ck.getCId()
            + PopConstants.SPLIT + ck.getPopTime()
            + PopConstants.SPLIT + ck.getBrokerName()
            + PopConstants.SPLIT + PopConstants.CK_TAG;
    }
}
