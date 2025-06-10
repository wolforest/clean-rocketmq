package cn.coderule.minimq.store.domain.mq.revive;

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
import cn.coderule.minimq.domain.domain.enums.message.MessageStatus;
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
import java.util.Map;
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

    private ReviveConsumer reviveConsumer;
    private RetryService retryService;

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

    public void setSkipRevive(boolean skip) {
        this.skipRevive = skip;
        reviveConsumer.setSkipRevive(skip);
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
            ReviveBuffer buffer = reviveConsumer.consume(reviveOffset);
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

    private boolean shouldSkip() {
        return skipRevive;
    }
}
