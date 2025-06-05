package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveBuffer;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.MQService;
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

        consumeOffsetService.putOffset(PopConstants.REVIVE_GROUP, reviveTopic, queueId, reviveOffset);
    }

    private ReviveBuffer consume() {
        ReviveBuffer reviveBuffer = new ReviveBuffer(reviveOffset);

        while (true) {
            if (skipRevive) {
                break;
            }

            List<MessageBO> messageList = pullMessage();
            long now = System.currentTimeMillis();
            if (CollectionUtil.isEmpty(messageList)) {
                if (!handleEmptyMessage(reviveBuffer, now)) {
                    break;
                }
                continue;
            }

            reviveBuffer.setNoMsgCount(0);
            long elapsedTime = now - reviveBuffer.getStartTime();
            if (elapsedTime > messageConfig.getReviveScanTime()) {
                log.info("revive scan time out, topic={}; reviveQueueId={}", reviveTopic, queueId);
                break;
            }

            parseMessage(reviveBuffer, messageList);
        }

        return reviveBuffer;
    }

    private void revive(ReviveBuffer reviveBuffer) {
        ArrayList<PopCheckPoint> checkPointList = reviveBuffer.getSortedList();
    }

    private void parseMessage(ReviveBuffer buffer, List<MessageBO> messageList) {

    }

    private boolean handleEmptyMessage(ReviveBuffer buffer, long now) {
        buffer.setMaxDeliverTime(now);

        if (buffer.getMaxDeliverTime() - buffer.getFirstReviveTime() > PopConstants.ackTimeInterval + 1000) {
            return false;
        }
        buffer.increaseNoMsgCount();
        return buffer.getNoMsgCount() * 100 <= 4000;
    }

    private List<MessageBO> pullMessage() {
        DequeueResult result = mqService.dequeue(
            PopConstants.REVIVE_GROUP,
            reviveTopic,
            queueId,
            32
        );

        return result.getMessageList();
    }

    private boolean shouldSkip() {
        return skipRevive;
    }
}
