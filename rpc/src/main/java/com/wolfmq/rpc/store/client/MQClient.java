package com.wolfmq.rpc.store.client;

import com.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import com.wolfmq.domain.domain.consumer.ack.store.AckMessage;
import com.wolfmq.domain.domain.consumer.ack.store.CheckPointRequest;
import com.wolfmq.domain.domain.consumer.ack.store.OffsetRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.MessageResult;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueRequest;
import com.wolfmq.domain.domain.consumer.consume.mq.QueueResult;
import com.wolfmq.domain.domain.mq.dto.EnqueueRequest;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import com.wolfmq.domain.domain.mq.dto.DequeueRequest;
import com.wolfmq.domain.domain.mq.dto.DequeueResult;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.StoreClient;
import com.wolfmq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;

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
    public MessageResult getMessage(MessageRequest request) {
        return null;
    }

    @Override
    public void addCheckPoint(CheckPointRequest request) {

    }

    @Override
    public void ack(AckMessage request) {

    }

    @Override
    public AckResult changeInvisible(AckMessage request) {
        return null;
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
