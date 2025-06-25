package cn.coderule.minimq.domain.service.broker.infra;

import cn.coderule.minimq.domain.domain.consumer.consume.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.DequeueResult;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import java.util.concurrent.CompletableFuture;

public interface MQStore {
    EnqueueResult enqueue(EnqueueRequest request);
    CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request);

    DequeueResult dequeue(DequeueRequest request);
    CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request);

    DequeueResult get(DequeueRequest request);
}
