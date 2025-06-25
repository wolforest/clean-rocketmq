package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.message.MessageConfig;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.revive.ReviveBuffer;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;

/**
 * ReviveThread, each revive message queue will have a ReviveThread.
 * working flow:
 *  - consume revive message from revive queue,
 *  - revive message
 *
 */
@Slf4j
public class ReviveThread extends ServiceThread {
    private final MessageConfig messageConfig;
    private final String reviveTopic;
    private final int queueId;

    private final Reviver reviver;
    private final ReviveConsumer consumer;
    private final OffsetService offsetService;

    private volatile boolean skipRevive = false;

    public ReviveThread(ReviveContext context, int queueId, RetryService retryService) {
        this.messageConfig = context.getMessageConfig();
        this.reviveTopic = context.getReviveTopic();
        this.queueId = queueId;

        this.reviver = new Reviver(context, queueId, retryService);
        this.consumer = new ReviveConsumer(context, queueId);
        this.offsetService = new OffsetService(context, queueId);
    }

    public void setSkipRevive(boolean skip) {
        this.skipRevive = skip;
        consumer.setSkipRevive(skip);
        reviver.setSkipRevive(skip);
        offsetService.setSkipRevive(skip);
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

            offsetService.initOffset();
            ReviveBuffer buffer = consumer.consume(offsetService.getReviveOffset());
            if (skipRevive) {
                log.info("skip revive topic={}; reviveQueueId={}",
                    reviveTopic, queueId);
                continue;
            }

            reviver.revive(buffer);
            offsetService.resetOffset(buffer);
            counter = calculateAndWait(buffer, counter);
        }
    }

    private int initCounter(ReviveBuffer buffer, int counter) {
        ArrayList<PopCheckPoint> pointList = buffer.getSortedList();
        long delay = 0;
        long now = System.currentTimeMillis();

        if (CollectionUtil.isEmpty(pointList)) {
            offsetService.setReviveTimestamp(now);
        } else {
            long firstReviveTime = pointList.get(0).getReviveTime();
            offsetService.setReviveTimestamp(firstReviveTime);
            delay = (now - firstReviveTime) / 1000;
            counter = 1;
        }

        log.info("revive finished, topic={}; reviveQueueId={}; startOffset={}; endOffset={}, delay={};",
            reviveTopic, queueId, buffer.getInitialOffset(), buffer.getOffset(), delay);

        return counter;
    }

    private int calculateAndWait(ReviveBuffer buffer, int counter) {
        counter = initCounter(buffer, counter);

        ArrayList<PopCheckPoint> pointList = buffer.getSortedList();
        if (!CollectionUtil.isEmpty(pointList)) {
            return counter;
        }

        long interval = counter * messageConfig.getReviveInterval();
        this.await(interval);

        if (counter < messageConfig.getReviveMaxSlow()) {
            counter++;
        }

        return counter;
    }

    private boolean shouldSkip() {
        return skipRevive;
    }
}
