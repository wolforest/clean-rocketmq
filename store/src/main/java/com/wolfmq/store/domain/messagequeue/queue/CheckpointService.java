package com.wolfmq.store.domain.messagequeue.queue;

import com.wolfmq.common.domain.messagequeue.dto.DequeueRequest;
import com.wolfmq.common.domain.messagequeue.dto.DequeueResult;
import com.wolfmq.common.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import com.wolfmq.common.domain.consumer.consume.pop.helper.PopConverter;
import com.wolfmq.store.domain.messagequeue.ack.AckService;

@Deprecated
public class CheckpointService {
    private final AckService ackService;

    public CheckpointService(AckService ackService) {
        this.ackService = ackService;
    }

    public void add(DequeueRequest request, DequeueResult result) {
        PopCheckPoint checkPoint = PopConverter.toCheckPoint(request, result);
        ackService.addCheckPoint(
            checkPoint,
            request.getReviveQueueId(),
            -1,
            result.getNextOffset()
        );
    }

}
