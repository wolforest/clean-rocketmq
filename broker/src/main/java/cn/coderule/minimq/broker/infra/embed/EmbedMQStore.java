package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.consumer.ack.store.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.service.broker.infra.MQStore;
import java.util.concurrent.CompletableFuture;

public class EmbedMQStore extends AbstractEmbedStore implements MQStore {
    private final MQStore mqStore;
    public EmbedMQStore(MQStore mqStore, EmbedLoadBalance loadBalance) {
        super(loadBalance);
        this.mqStore = mqStore;
    }

    @Override
    public EnqueueResult enqueue(EnqueueRequest request) {
        return mqStore.enqueue(request);
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request) {
        return mqStore.enqueueAsync(request);
    }

    @Override
    public DequeueResult dequeue(DequeueRequest request) {
        return mqStore.dequeue(request);
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request) {
        return mqStore.dequeueAsync(request);
    }

    @Override
    public DequeueResult get(DequeueRequest request) {
        return mqStore.get(request);
    }

    @Override
    public void addCheckPoint(CheckPointRequest request) {
        mqStore.addCheckPoint(request);
    }

    @Override
    public void ack(AckRequest request) {
        mqStore.ack(request);
    }

    @Override
    public long getBufferedOffset(OffsetRequest request) {
        return mqStore.getBufferedOffset(request);
    }

    @Override
    public QueueResult getMinOffset(QueueRequest request) {
        return mqStore.getMinOffset(request);
    }

    @Override
    public QueueResult getMaxOffset(QueueRequest request) {
        return mqStore.getMaxOffset(request);
    }

}
