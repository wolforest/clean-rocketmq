package cn.coderule.minimq.broker.domain.consumer.revive;

import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.lang.ByteUtil;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.domain.domain.consumer.revive.ReviveBuffer;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.meta.offset.OffsetRequest;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.store.facade.ConsumeOffsetFacade;
import cn.coderule.minimq.rpc.store.facade.SubscriptionFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;
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
    private final BrokerConfig brokerConfig;
    private final RetryService retryService;

    private final MQFacade mqFacade;
    private final TopicFacade topicFacade;
    private final SubscriptionFacade subscriptionFacade;
    private final ConsumeOffsetFacade consumeOffsetFacade;

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
        this.brokerConfig = context.getBrokerConfig();
        this.reviveTopic = context.getReviveTopic();
        this.queueId = queueId;

        this.retryService = retryService;

        this.mqFacade = context.getMqFacade();
        this.topicFacade = context.getTopicFacade();
        this.subscriptionFacade = context.getSubscriptionFacade();
        this.consumeOffsetFacade = context.getConsumeOffsetFacade();

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
        if (!topicFacade.exists(topic)) {
            log.warn("reviveQueueId={}, can not get normal topic {}, then continue", queueId, popCheckPoint.getTopic());
            return false;
        }

        if (!subscriptionFacade.existsGroup(popCheckPoint.getTopic(), popCheckPoint.getCId())) {
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

        SocketAddress storeHost = new InetSocketAddress(brokerConfig.getHost(), brokerConfig.getPort());
        MessageBO message = PopConverter.toMessage(newCheckPoint, queueId, reviveTopic, storeHost);

        mqFacade.enqueue(EnqueueRequest.create(message));
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

    /**
     * revive message
     *  - enqueue original message
     *  - or enqueue checkpoint message
     * @param point check point
     */
    private void reviveMessage(PopCheckPoint point) {
        for (int i = 0; i < point.getNum(); i++) {
            // skip, if the message has been acked
            if (ByteUtil.getBit(point.getBitMap(), i)) continue;

            long offset = point.ackOffsetByIndex((byte) i);

            DequeueRequest request = DequeueRequest.builder()
                    .topic(point.getTopic())
                    .queueId(point.getQueueId())
                    .offset(offset)
                    .num(1)
                    .build();

            DequeueResult result = mqFacade.get(request);

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
        OffsetRequest request = OffsetRequest.builder()
            .consumerGroup(PopConstants.REVIVE_GROUP)
            .topicName(reviveTopic)
            .queueId(queueId)
            .newOffset(offset)
            .build();

        consumeOffsetFacade.putOffset(request);
    }
}
