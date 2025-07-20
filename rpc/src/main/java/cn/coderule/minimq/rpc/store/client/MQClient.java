package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.domain.consumer.ack.store.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.CheckPointRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.store.OffsetRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.QueueResult;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;
import lombok.Setter;

@Setter
public class MQClient extends AbstractStoreClient implements StoreClient, MQFacade {

    public MQClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
    }

    @Override
    public EnqueueResult enqueue(EnqueueRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(EnqueueRequest request) {
        return null;
    }

    @Override
    public DequeueResult dequeue(DequeueRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(DequeueRequest request) {
        return null;
    }

    @Override
    public DequeueResult get(DequeueRequest request) {

        return null;
    }

    @Override
    public void addCheckPoint(CheckPointRequest request) {

    }

    @Override
    public void ack(AckRequest request) {

    }

    @Override
    public long getBufferedOffset(OffsetRequest request) {
        return 0;
    }

    @Override
    public QueueResult getMinOffset(QueueRequest request) {
        return null;
    }

    @Override
    public QueueResult getMaxOffset(QueueRequest request) {
        return null;
    }

}
