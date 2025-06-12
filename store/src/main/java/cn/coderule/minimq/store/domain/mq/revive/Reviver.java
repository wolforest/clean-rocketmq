package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.lang.ByteUtil;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.domain.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.consumer.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveBuffer;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.SubscriptionService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Reviver {
    private final StoreConfig storeConfig;
    private final MQService mqService;
    private final RetryService retryService;

    private final TopicService topicService;
    private final SubscriptionService subscriptionService;
    private final ConsumeOffsetService consumeOffsetService;

    private final String reviveTopic;
    private final int queueId;
    @Setter
    private volatile boolean skipRevive = false;

    /**
     * checkpoint -> (timestamp, result)
     * timestamp: when check point was revived
     * result:
     *   - default value is false
     *   - true: original message
     *           or checkpoint message was resend
     */
    private final NavigableMap<PopCheckPoint, Pair<Long, Boolean>> inflightMap;

    public Reviver(ReviveContext context, int queueId, RetryService retryService) {
        this.storeConfig = context.getStoreConfig();
        this.reviveTopic = context.getReviveTopic();
        this.queueId = queueId;

        this.mqService = context.getMqService();
        this.retryService = retryService;

        this.topicService = context.getTopicService();
        this.subscriptionService = context.getSubscriptionService();
        this.consumeOffsetService = context.getConsumeOffsetService();

        this.inflightMap = Collections.synchronizedNavigableMap(new TreeMap<>());
    }

    public void revive(ReviveBuffer reviveBuffer) {
        long tmpOffset = reviveBuffer.getInitialOffset();
        // sort by reviveOffset
        ArrayList<PopCheckPoint> pointList = reviveBuffer.getSortedList();

        for (PopCheckPoint point : pointList) {
            // break if checkpoint.reviveTime <= maxReviveTime - 2s
            if (shouldStop(reviveBuffer, point)) break;

            if (!existsTopicAndSubscription(point)) {
                tmpOffset  = point.getReviveOffset();
                continue;
            }

            reputExpiredCheckpoint();
            reviveFromCheckpoint(point);
        }

        reviveBuffer.setOffset(tmpOffset);
    }

    /**
     *
     * ReviveBuffer.maxDeliverTime = max(MessageBO.deliverTime)
     * MessageBO.deliverTime =
     *  - checkpoint.popTime + checkpoint.invisibleTime
     *  - reviveTime - PopConstants.ackTimeInterval
     *  - reviveTime
     * CheckPoint.reviveTime = popTime + invisibleTime
     * @param buffer buffer
     * @param point check point
     * @return boolean
     */
    private boolean shouldStop(ReviveBuffer buffer, PopCheckPoint point) {
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
            ThreadUtil.sleep(100);

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

        // enqueue original message or enqueue checkpoint related message
        reviveMessage(point);

        inflightMap.get(point).setRight(true);
        clearInflightMap();
    }

    private void clearInflightMap() {
        for (Map.Entry<PopCheckPoint, Pair<Long, Boolean>> entry : inflightMap.entrySet()) {
            Pair<Long, Boolean> pair = entry.getValue();
            if (!pair.getRight()) {
                break;
            }

            PopCheckPoint point = entry.getKey();
            commitOffset(point.getReviveOffset());
            inflightMap.remove(point);
        }
    }

    private void reviveMessage(PopCheckPoint point) {
        for (int i = 0; i < point.getNum(); i++) {
            // skip, if the message has been acked
            if (ByteUtil.getBit(point.getBitMap(), i)) continue;

            long offset = point.ackOffsetByIndex((byte) i);
            DequeueResult result = mqService.get(point.getTopic(), point.getQueueId(), offset);

            boolean isSuccess = retryOriginalMessage(point, result);
            if (isSuccess) {
                continue;
            }

            reputCheckpoint(point, offset);
        }
    }

    private boolean parseDequeueStatus(MessageStatus status) {
        return switch (status) {
            case MESSAGE_WAS_REMOVING,
                 OFFSET_TOO_SMALL,
                 NO_MATCHED_LOGIC_QUEUE,
                 NO_MESSAGE_IN_QUEUE -> true;
            default -> false;
        };
    }

    private boolean retryOriginalMessage(PopCheckPoint point, DequeueResult result) {
        if (result.isEmpty()) {
            return parseDequeueStatus(result.getStatus());
        }

        return retryService.retry(point, result.getMessage());
    }

    private void commitOffset(long offset) {
        consumeOffsetService.putOffset(
            PopConstants.REVIVE_GROUP,
            reviveTopic,
            queueId,
            offset
        );
    }
}
