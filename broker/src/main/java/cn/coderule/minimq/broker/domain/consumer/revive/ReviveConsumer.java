package cn.coderule.minimq.broker.domain.consumer.revive;

import cn.coderule.common.util.lang.ByteUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.minimq.domain.domain.consumer.ack.BatchAckInfo;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopKeyBuilder;
import cn.coderule.minimq.domain.domain.consumer.revive.ReviveBuffer;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import java.util.List;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Revive message consumer, it's stateless
 *  - pull message from revive topic
 *  - convert message to checkpoint/ack/batchAck
 *  - merge ack/batchAck with checkpoint
 */
@Slf4j
public class ReviveConsumer {
    private final MessageConfig messageConfig;

    @Setter
    private volatile boolean skipRevive = false;

    private final String reviveTopic;
    private final int queueId;

    private final MQFacade mqFacade;

    public ReviveConsumer(ReviveContext context, int queueId) {
        this.messageConfig = context.getMessageConfig();
        this.reviveTopic = context.getReviveTopic();
        this.queueId = queueId;

        this.mqFacade = context.getMqFacade();
    }

    public ReviveBuffer consume(long offset) {
        ReviveBuffer buffer = new ReviveBuffer(offset);

        while (true) {
            if (skipRevive) break;

            List<MessageBO> messageList = pullMessage(buffer.getOffset());

            long now = System.currentTimeMillis();
            if (CollectionUtil.isEmpty(messageList)) {
                if (!handleEmptyMessage(buffer, now)) {
                    break;
                }
                continue;
            }

            parseMessage(buffer, messageList);

            if (isTimeout(buffer, now)) break;
        }

        buffer.mergeAckMap();

        return buffer;
    }

    private boolean isTimeout(ReviveBuffer buffer, long now) {
        long elapsedTime = now - buffer.getStartTime();
        if (elapsedTime > messageConfig.getReviveScanTime()) {
            log.info("revive scan timeout, topic={}; reviveQueueId={}", reviveTopic, queueId);
            return true;
        }

        return false;
    }

    private void parseMessage(ReviveBuffer buffer, List<MessageBO> messageList) {
        buffer.setNoMsgCount(0);
        buffer.setOffset(buffer.getOffset() + messageList.size());

        for (MessageBO message : messageList) {
            if (!parseByTag(buffer, message)) continue;

            long deliverTime = message.getDeliverTime();
            if (deliverTime > buffer.getMaxDeliverTime()) {
                buffer.setMaxDeliverTime(deliverTime);
            }
        }
    }

    private boolean parseByTag(ReviveBuffer buffer, MessageBO message) {
        if (PopConstants.CK_TAG.equals(message.getTags())) {
            return parseCheckPoint(buffer, message);
        }

        if (PopConstants.ACK_TAG.equals(message.getTags())) {
            return parseAck(buffer, message);
        }

        if (PopConstants.BATCH_ACK_TAG.equals(message.getTags())) {
            return parseBatchAck(buffer, message);
        }

        return false;
    }

    private boolean parseCheckPoint(ReviveBuffer buffer, MessageBO message) {
        PopCheckPoint point = JSONUtil.parse(message.getStringBody(), PopCheckPoint.class);
        if (messageConfig.isEnablePopLog()) {
            log.info("find checkpoint, reviveQueueId={}, offset={}, checkpoint={}",
                message.getQueueId(), message.getQueueOffset(), point);
        }

        if (null == point.getTopic() || null == point.getCId()) {
            return false;
        }

        buffer.addCheckPoint(point);
        point.setReviveOffset(message.getQueueOffset());

        if (0 == buffer.getFirstReviveTime()) {
            buffer.setFirstReviveTime(point.getReviveTime());
        }

        return true;
    }

    private boolean parseAck(ReviveBuffer buffer, MessageBO message) {
        AckInfo ackInfo = JSONUtil.parse(message.getStringBody(), AckInfo.class);
        if (messageConfig.isEnablePopLog()) {
            log.info("find ack, reviveQueueId={}, offset={}, ackMsg={}",
                message.getQueueId(), message.getQueueOffset(), ackInfo);
        }

        String mergeKey = PopKeyBuilder.buildReviveKey(ackInfo);
        PopCheckPoint point = buffer.getCheckPoint(mergeKey);
        if (point == null) {
            return addAckMsg(buffer, ackInfo, message, mergeKey);
        }

        mergeAckMsg(ackInfo, point);
        return true;
    }

    private boolean parseBatchAck(ReviveBuffer buffer, MessageBO message) {
        BatchAckInfo batchAckMsg = JSONUtil.parse(message.getStringBody(), BatchAckInfo.class);
        if (messageConfig.isEnablePopLog()) {
            log.info("find batch ack, reviveQueueId={}, offset={}, batchAckMsg={}",
                message.getQueueId(), message.getQueueOffset(), batchAckMsg);
        }

        String mergeKey = PopKeyBuilder.buildReviveKey(batchAckMsg);
        PopCheckPoint point = buffer.getCheckPoint(mergeKey);

        if (point == null) {
            return addAckMsg(buffer, batchAckMsg, message, mergeKey);
        }

        mergeBatchAckMsg(batchAckMsg, point);
        return true;
    }

    private boolean addAckMsg(ReviveBuffer buffer, AckInfo ackInfo, MessageBO message, String mergeKey) {
        if (!messageConfig.isEnableSkipLongAwaitingAck()) {
            return false;
        }

        long ackWaitTime = System.currentTimeMillis() - message.getDeliverTime();
        if (ackWaitTime <= messageConfig.getReviveAckWaitMs()) {
            return true;
        }

        PopCheckPoint point = PopConverter.toCheckPoint(ackInfo, message.getQueueOffset());
        buffer.addAck(mergeKey, point);
        log.warn("can't find checkpoint for ack, waitTime={}ms, mergeKey={}, ack={}, mockCheckPoint={}",
            ackWaitTime, mergeKey, ackInfo, point);

        if (0 == buffer.getFirstReviveTime()) {
            buffer.setFirstReviveTime(point.getReviveTime());
        }

        return true;
    }

    private void mergeBatchAckMsg(BatchAckInfo batchAckMsg, PopCheckPoint point) {
        List<Long> offsetList = batchAckMsg.getAckOffsetList();
        for (Long offset : offsetList) {
            int index = point.indexOfAck(offset);

            if (index < 0) {
                log.error("invalid index of BatchAckMsg, ackMsg: {}, index: {}, checkPoint: {}",
                    batchAckMsg, index, point);
                continue;
            }

            int bitMap = ByteUtil.setBit(point.getBitMap(), index, true);
            point.setBitMap(bitMap);
        }
    }

    private void mergeAckMsg(AckInfo ackInfo, PopCheckPoint point) {
        int index = point.indexOfAck(ackInfo.getAckOffset());
        if (index < 0) {
            log.error("invalid ack index, ackMsg={}, point={}", ackInfo, point);
            return;
        }

        int newBitMap = ByteUtil.setBit(point.getBitMap(), index, true);
        point.setBitMap(newBitMap);
    }

    private boolean handleEmptyMessage(ReviveBuffer buffer, long now) {
        buffer.setMaxDeliverTime(now);

        long time = buffer.getMaxDeliverTime() - buffer.getFirstReviveTime();
        if (time > PopConstants.ackTimeInterval + 1000) {
            return false;
        }

        buffer.increaseNoMsgCount();
        return buffer.getNoMsgCount() * 100 <= 4000;
    }

    private List<MessageBO> pullMessage(long reviveOffset) {
        DequeueRequest request = DequeueRequest.builder()
            .group(PopConstants.REVIVE_GROUP)
            .topic(reviveTopic)
            .queueId(queueId)
            .offset(reviveOffset)
            .maxNum(32)
            .build();

        DequeueResult result = mqFacade.get(request);

        return result.getMessageList();
    }

}
