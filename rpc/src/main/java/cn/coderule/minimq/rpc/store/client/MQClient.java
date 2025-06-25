package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.consumer.consume.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.DequeueResult;
import cn.coderule.minimq.domain.service.store.api.MQStore;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Setter;

@Setter
public class MQClient extends AbstractStoreClient implements StoreClient, MQStore {

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
    public MessageBO getMessage(String topic, int queueId, long offset) {

        return null;
    }

    @Override
    public List<MessageBO> getMessage(String topic, int queueId, long offset, int num) {


        return List.of();
    }

}
