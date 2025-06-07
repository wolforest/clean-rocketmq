package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.lang.ByteUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.domain.dto.DequeueRequest;
import cn.coderule.minimq.domain.domain.model.consumer.pop.ack.AckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.ack.BatchAckMsg;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.consumer.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.model.consumer.pop.helper.PopKeyBuilder;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveBuffer;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReviveThread extends ServiceThread {
    private final StoreConfig storeConfig;
    private final MessageConfig messageConfig;
    private final String reviveTopic;
    private final int queueId;
    private long reviveOffset;

    private final ConsumeOffsetService consumeOffsetService;
    private final MQService mqService;
    private TopicService topicService;
    private SubscriptionService subscriptionService;

    private volatile boolean skipRevive = false;
    /**
     * checkpoint -> (msgOffset, retryResult)
     */
    private final NavigableMap<PopCheckPoint, Pair<Long, Boolean>> inflightMap;

    public ReviveThread(
        StoreConfig  storeConfig,
        String reviveTopic,
        int queueId,
        MQService mqService,
        ConsumeOffsetService consumeOffsetService
    ) {
        this.storeConfig = storeConfig;
        this.messageConfig = storeConfig.getMessageConfig();
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
        int counter = 1;
        while (!this.isStopped()) {
            if (shouldSkip()) continue;

            initOffset();
            ReviveBuffer buffer = consume();
            if (skipRevive) {
                log.info("skip revive topic={}; reviveQueueId={}",
                    reviveTopic, queueId);
                continue;
            }

            revive(buffer);
            resetOffset(buffer);
            counter = calculateAndWait(buffer, counter);
        }
    }

    private void initOffset() {
        log.info("start revive topic={}; reviveQueueId={}",
            reviveTopic, queueId);

        reviveOffset = consumeOffsetService.getOffset(
            PopConstants.REVIVE_GROUP,
            reviveTopic,
            queueId
        );
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
        consumeOffsetService.putOffset(
            PopConstants.REVIVE_GROUP,
            reviveTopic,
            queueId,
            offset
        );
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
            buffer.setNoMsgCount(0);
            buffer.setOffset(buffer.getOffset() + messageList.size());

            if (isTimeout(buffer, now)) break;
        }

        buffer.mergeAckMap();
        return buffer;
    }

    private boolean shouldSkip(ReviveBuffer buffer, PopCheckPoint point) {
        if (skipRevive) {
            log.info("skip revive topic={}, reviveQueueId={}", reviveTopic, queueId);
            return true;
        }

        long timeSpan = buffer.getMaxDeliverTime() - point.getReviveTime();
        return timeSpan <= PopConstants.ackTimeInterval + 1_000;
    }

    private boolean existsTopicAndSubscription(PopCheckPoint popCheckPoint) {
        // check normal topic, skip ck , if normal topic is not exist
        String topic = KeyBuilder.removeRetryPrefix(popCheckPoint.getTopic(), popCheckPoint.getCId());
        if (!topicService.exists(topic)) {
            log.warn("reviveQueueId={}, can not get normal topic {}, then continue", queueId, popCheckPoint.getTopic());
            return false;
        }

        if (!subscriptionService.existsGroup(popCheckPoint.getCId())) {
            log.warn("reviveQueueId={}, can not get cid {}, then continue", queueId, popCheckPoint.getCId());
            return false;
        }

        return true;
    }

    private void reputExpiredCheckpoint() {
        while (inflightMap.size() > 3) {
            await(100);

            Pair<Long, Boolean> pair = inflightMap.firstEntry().getValue();
            // if checkpoint result is true, continue
            if (pair.getRight()) {
                continue;
            }

            long now = System.currentTimeMillis();
            if (now - pair.getLeft() <= 30_000) {
                continue;
            }

            PopCheckPoint firstPoint = inflightMap.firstKey();
            reputCheckpoint(firstPoint, pair.getLeft());
            inflightMap.remove(firstPoint);
        }
    }

    private void reputCheckpoint(PopCheckPoint point, long startOffset) {
        PopCheckPoint newCheckPoint = PopConverter.toCheckPoint(point, startOffset);

        SocketAddress storeHost = new InetSocketAddress(storeConfig.getHost(), storeConfig.getPort());
        MessageBO message = PopConverter.toMessage(newCheckPoint, queueId, reviveTopic, storeHost);

        mqService.enqueue(message);
    }

    private void reviveFromCheckpoint(PopCheckPoint point) {
        if (skipRevive) {
            log.info("skip revive from reviveFromCheckpoint topic={}, reviveQueueId={}",
                reviveTopic, queueId);
            return;
        }

        inflightMap.put(
            point,
            Pair.of(System.currentTimeMillis(), false)
        );
    }


    private void revive(ReviveBuffer reviveBuffer) {
        long tmpOffset = reviveBuffer.getOffset();
        ArrayList<PopCheckPoint> pointList = reviveBuffer.getSortedList();

        for (PopCheckPoint point : pointList) {
            if (shouldSkip(reviveBuffer, point)) break;

            if (!existsTopicAndSubscription(point)) {
                tmpOffset  = point.getReviveOffset();
                continue;
            }

            reputExpiredCheckpoint();
            reviveFromCheckpoint(point);
        }

        reviveBuffer.setOffset(tmpOffset);
    }

    private int calculateAndWait(ReviveBuffer buffer, int counter) {
        return counter;
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
            return addAckMsg(buffer, ackMsg, message, mergeKey);
        }

        mergeAckMsg(ackMsg, point);
        return true;
    }

    private boolean parseBatchAck(ReviveBuffer buffer, MessageBO message) {
        BatchAckMsg batchAckMsg = JSONUtil.parse(message.getStringBody(), BatchAckMsg.class);
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

    private boolean addAckMsg(ReviveBuffer buffer, AckMsg ackMsg, MessageBO message, String mergeKey) {
        if (!messageConfig.isEnableSkipLongAwaitingAck()) {
            return false;
        }

        long ackWaitTime = System.currentTimeMillis() - message.getDeliverTime();
        if (ackWaitTime <= messageConfig.getReviveAckWaitMs()) {
            return true;
        }

        PopCheckPoint point = PopConverter.toCheckPoint(ackMsg, message.getQueueOffset());
        buffer.addAck(mergeKey, point);
        log.warn("can't find checkpoint for ack, waitTime={}ms, mergeKey={}, ack={}, mockCheckPoint={}",
            ackWaitTime, mergeKey, ackMsg, point);

        if (0 == buffer.getFirstReviveTime()) {
            buffer.setFirstReviveTime(point.getReviveTime());
        }

        return true;
    }

    private void mergeBatchAckMsg(BatchAckMsg batchAckMsg, PopCheckPoint point) {
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

    private void mergeAckMsg(AckMsg ackMsg, PopCheckPoint point) {
        int index = point.indexOfAck(ackMsg.getAckOffset());
        if (index < 0) {
            log.error("invalid ack index, ackMsg={}, point={}", ackMsg, point);
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
            .num(32)
            .build();

        DequeueResult result = mqService.get(request);

        return result.getMessageList();
    }

    private boolean shouldSkip() {
        return skipRevive;
    }
}
