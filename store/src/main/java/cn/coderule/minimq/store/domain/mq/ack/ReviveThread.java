package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.lang.type.Pair;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
import cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveContext;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveMap;
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

            log.info("start revive topic={}; reviveQueueId={}", reviveTopic, queueId);
            ReviveMap reviveMap = consumeReviveObj();
            if (skipRevive) {
                log.info("skip revive topic={}; reviveQueueId={}", reviveTopic, queueId);
                continue;
            }

            revive(reviveMap);
        }
    }

    private ReviveMap consumeReviveObj() {
        ReviveContext context = new ReviveContext();

        while (true) {
            if (skipRevive) {
                break;
            }

            List<MessageBO> messageList = pullMessage();
            long now = System.currentTimeMillis();
            if (CollectionUtil.isEmpty(messageList)) {
                if (!handleEmptyMessage(context, now)) {
                    break;
                }
                continue;
            }

            context.setNoMsgCount(0);
            long elapsedTime = now - context.getStartTime();
            if (elapsedTime > messageConfig.getReviveScanTime()) {
                log.info("revive scan time out, topic={}; reviveQueueId={}", reviveTopic, queueId);
                break;
            }

            parseMessage(context, messageList);
        }

        return context.getReviveMap();
    }

    private void parseMessage(ReviveContext context, List<MessageBO> messageList) {

    }

    private boolean handleEmptyMessage(ReviveContext context, long now) {
        context.setEndTime(now);

        if (context.getEndTime() - context.getFirstRt() > PopConstants.ackTimeInterval + 1000) {
            return false;
        }
        context.increaseNoMsgCount();
        return context.getNoMsgCount() * 100 <= 4000;
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

    private void revive(ReviveMap reviveMap) {
        ArrayList<PopCheckPoint> checkPointList = reviveMap.getSortedList();
    }

    private boolean shouldSkip() {
        return false;
    }
}
