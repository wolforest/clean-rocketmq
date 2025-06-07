package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.domain.dto.DequeueRequest;
import cn.coderule.minimq.domain.domain.model.consumer.pop.ack.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.consumer.pop.helper.PopKeyBuilder;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveBuffer;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReviveThread extends ServiceThread {
    private final MessageConfig messageConfig;
    private final String reviveTopic;
    private final int queueId;
    private long reviveOffset;

    private final ConsumeOffsetService consumeOffsetService;
    private final MQService mqService;

    private volatile boolean skipRevive = false;
    /**
     * checkpoint -> (msgOffset, retryResult)
     */
    private final NavigableMap<PopCheckPoint, Pair<Long, Boolean>> inflightMap;

    public ReviveThread(
        MessageConfig messageConfig,
        String reviveTopic,
        int queueId,
        MQService mqService,
        ConsumeOffsetService consumeOffsetService
    ) {
        this.messageConfig = messageConfig;
        this.reviveTopic = reviveTopic;
        this.queueId = queueId;

        this.mqService = mqService;
        this.consumeOffsetService = consumeOffsetService;

        this.inflightMap = Collections.synchronizedNavigableMap(new TreeMap<>());
    }

    @Override
    public String getServiceName() {
        return ReviveThread.class.getSimpleName();
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            if (shouldSkip()) continue;

            initOffset();
            ReviveBuffer buffer = consume();
            if (skipRevive) {
                log.info("skip revive topic={}; reviveQueueId={}", reviveTopic, queueId);
                continue;
            }

            revive(buffer);
            resetOffset(buffer);
        }
    }

    private void initOffset() {
        log.info("start revive topic={}; reviveQueueId={}", reviveTopic, queueId);
        reviveOffset = consumeOffsetService.getOffset(PopConstants.REVIVE_GROUP, reviveTopic, queueId);
    }

    private void resetOffset(ReviveBuffer buffer) {
        if (skipRevive) {
            return;
        }

        reviveOffset = buffer.getOffset();

        if (buffer.getOffset() <= buffer.getInitialOffset()) {
            return;
        }

        commitOffset(reviveOffset);
    }

    private void commitOffset(long offset) {
        consumeOffsetService.putOffset(PopConstants.REVIVE_GROUP, reviveTopic, queueId, offset);
    }

    private ReviveBuffer consume() {
        ReviveBuffer buffer = new ReviveBuffer(reviveOffset);

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

            if (isExpired(buffer, now)) break;
        }

        return buffer;
    }

    private void revive(ReviveBuffer reviveBuffer) {
        ArrayList<PopCheckPoint> checkPointList = reviveBuffer.getSortedList();
    }

    private boolean isExpired(ReviveBuffer buffer, long now) {
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
        AckMsg ackMsg = JSONUtil.parse(message.getStringBody(), AckMsg.class);
        if (messageConfig.isEnablePopLog()) {
            log.info("find ack, reviveQueueId={}, offset={}, ackMsg={}",
                message.getQueueId(), message.getQueueOffset(), ackMsg);
        }

        String mergeKey = PopKeyBuilder.buildReviveKey(ackMsg);
        PopCheckPoint point = buffer.getCheckPoint(mergeKey);
        if (point == null) {
            return addAckMsg(buffer, ackMsg, message);
        }

        return mergeAckMsg(buffer, ackMsg, point, message);
    }

    private boolean parseBatchAck(ReviveBuffer buffer, MessageBO message) {
        return true;
    }

    private boolean addAckMsg(ReviveBuffer buffer, AckMsg ackMsg, MessageBO message) {
        return true;
    }

    private boolean mergeAckMsg(ReviveBuffer buffer, AckMsg ackMsg, PopCheckPoint point, MessageBO message) {
        return true;
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
            .num(32)
            .build();

        DequeueResult result = mqService.get(request);

        return result.getMessageList();
    }

    private boolean shouldSkip() {
        return skipRevive;
    }
}
