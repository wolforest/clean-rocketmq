package cn.coderule.minimq.store.domain.mq.message;

import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.GetResult;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.service.store.domain.mq.MQService;
import java.util.concurrent.CompletableFuture;

public class DefaultMQService implements MQService {
    @Override
    public EnqueueResult enqueue(MessageBO messageBO) {
        return null;
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {
        return null;
    }

    @Override
    public GetResult dequeue(String topic, int queueId, int num) {
        return null;
    }
}
