package cn.coderule.wolfmq.broker.infra.embed;

import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.wolfmq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.wolfmq.rpc.store.facade.MQFacade;
import cn.coderule.wolfmq.domain.domain.store.api.MQStore;
import java.util.concurrent.CompletableFuture;

public class EmbedMQStore extends AbstractEmbedStore implements MQFacade {
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
    public MessageResult getMessage(MessageRequest request) {
        return mqStore.getMessage(request);
    }

    @Override
    public void addCheckPoint(CheckPointRequest request) {
        mqStore.addCheckPoint(request);
    }

    @Override
    public void ack(AckMessage request) {
        mqStore.ack(request);
    }

    @Override
    public AckResult changeInvisible(AckMessage request) {
        return mqStore.changeInvisible(request);
    }

    @Override
    public long getBufferedOffset(OffsetRequest request) {
        return mqStore.getBufferedOffset(request).getOffset();
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
