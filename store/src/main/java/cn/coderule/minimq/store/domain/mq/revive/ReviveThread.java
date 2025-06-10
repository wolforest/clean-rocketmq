package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveBuffer;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReviveThread extends ServiceThread {
    private final String reviveTopic;
    private final int queueId;
    private long reviveOffset;

    private final Reviver reviver;
    private final ReviveConsumer consumer;
    private final ConsumeOffsetService consumeOffsetService;

    private volatile boolean skipRevive = false;

    public ReviveThread(ReviveContext context, int queueId, Reviver reviver, ReviveConsumer consumer) {
        this.reviveTopic = context.getReviveTopic();
        this.queueId = queueId;

        this.reviver = reviver;
        this.consumer = consumer;

        this.consumeOffsetService = context.getConsumeOffsetService();
    }

    public void setSkipRevive(boolean skip) {
        this.skipRevive = skip;
        consumer.setSkipRevive(skip);
        reviver.setSkipRevive(skip);
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
            ReviveBuffer buffer = consumer.consume(reviveOffset);
            if (skipRevive) {
                log.info("skip revive topic={}; reviveQueueId={}",
                    reviveTopic, queueId);
                continue;
            }

            reviver.revive(buffer);
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

    private int calculateAndWait(ReviveBuffer buffer, int counter) {
        return counter;
    }

    private boolean shouldSkip() {
        return skipRevive;
    }
}
