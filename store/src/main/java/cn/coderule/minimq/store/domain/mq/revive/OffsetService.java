package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.minimq.domain.domain.core.constant.PopConstants;
import cn.coderule.minimq.domain.domain.model.consumer.pop.revive.ReviveBuffer;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OffsetService {
    private final String reviveTopic;
    private final int queueId;

    @Getter @Setter
    private long reviveOffset;

    @Getter @Setter
    private long reviveTimestamp = -1;
    @Setter
    private volatile boolean skipRevive = false;

    private final ConsumeOffsetService consumeOffsetService;
    private final ConsumeQueueGateway consumeQueueGateway;

    public OffsetService(ReviveContext context, int queueId) {
        this.reviveTopic = context.getReviveTopic();
        this.queueId = queueId;

        this.consumeOffsetService = context.getConsumeOffsetService();
        this.consumeQueueGateway = context.getConsumeQueueGateway();
    }

    public long getReviveDelayTime() {
        if (reviveTimestamp <= 0) {
            return 0;
        }

        long maxOffset = consumeQueueGateway.getMaxOffset(reviveTopic, queueId);
        if (maxOffset > reviveOffset + 1) {
            long now = System.currentTimeMillis();
            return Math.max(now, reviveTimestamp);
        }

        return 0;
    }

    public long getReviveDelayNumber() {
        if (reviveTimestamp <= 0) {
            return 0;
        }

        long maxOffset = consumeQueueGateway.getMaxOffset(reviveTopic, queueId);
        long diff = maxOffset - reviveOffset;
        return Math.max(diff, 0);
    }

    public void initOffset() {
        log.info("start revive topic={}; reviveQueueId={}",
            reviveTopic, queueId);

        reviveOffset = consumeOffsetService.getOffset(
            PopConstants.REVIVE_GROUP,
            reviveTopic,
            queueId
        );
    }

    public void resetOffset(ReviveBuffer buffer) {
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

}
