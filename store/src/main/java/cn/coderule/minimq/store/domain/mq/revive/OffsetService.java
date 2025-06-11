package cn.coderule.minimq.store.domain.mq.revive;

import cn.coderule.minimq.domain.config.MessageConfig;
import cn.coderule.minimq.domain.service.store.domain.consumequeue.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OffsetService {
    private final MessageConfig messageConfig;
    private final String reviveTopic;
    private final int queueId;
    private long reviveOffset;

    private long reviveTimestamp = -1;
    private volatile boolean skipRevive = false;

    private final ConsumeOffsetService consumeOffsetService;
    private final ConsumeQueueGateway consumeQueueGateway;

    public OffsetService(ReviveContext context, int queueId) {
        this.messageConfig = context.getMessageConfig();
        this.reviveTopic = context.getReviveTopic();
        this.queueId = queueId;

        this.consumeOffsetService = context.getConsumeOffsetService();
        this.consumeQueueGateway = context.getConsumeQueueGateway();
    }

}
