package cn.coderule.minimq.store.domain.mq.queue;

import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.helper.PopConverter;
import cn.coderule.minimq.store.domain.mq.ack.AckService;

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
