package cn.coderule.minimq.domain.domain.consumer.consume.pop.helper;

import cn.coderule.minimq.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.BatchAckInfo;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;

public class PopKeyBuilder {

    public static String buildLockKey(PopCheckPoint point) {
        return KeyBuilder.buildConsumeKey(point.getTopic(), point.getCId(), point.getQueueId());
    }

    public static String buildKey(AckInfo ackInfo) {
        return ackInfo.getTopic()
            + ackInfo.getConsumerGroup()
            + ackInfo.getQueueId()
            + ackInfo.getStartOffset()
            + ackInfo.getPopTime()
            + ackInfo.getBrokerName();
    }

    public static String buildKey(PopCheckPoint point) {
        return point.getTopic()
            + point.getCId()
            + point.getQueueId()
            + point.getStartOffset()
            + point.getPopTime()
            + point.getBrokerName();
    }

    public static String buildReviveKey(AckInfo ackInfo) {
        return ackInfo.getTopic()
            + ackInfo.getConsumerGroup()
            + ackInfo.getQueueId()
            + ackInfo.getStartOffset()
            + ackInfo.getPopTime();
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
    public static String genAckUniqueId(AckInfo ackInfo) {
        return ackInfo.getTopic()
            + PopConstants.SPLIT + ackInfo.getQueueId()
            + PopConstants.SPLIT + ackInfo.getAckOffset()
            + PopConstants.SPLIT + ackInfo.getConsumerGroup()
            + PopConstants.SPLIT + ackInfo.getPopTime()
            + PopConstants.SPLIT + ackInfo.getBrokerName()
            + PopConstants.SPLIT + PopConstants.ACK_TAG;
    }

    /**
     * moved from org.apache.rocketmq.broker.util.PopUtils
     */
    public static String genBatchAckUniqueId(BatchAckInfo batchAckMsg) {
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
