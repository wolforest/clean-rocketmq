package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.DequeueResult;
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
    public EnqueueResult enqueue(MessageBO messageBO) {


        return null;
    }

    @Override
    public CompletableFuture<EnqueueResult> enqueueAsync(MessageBO messageBO) {

        return null;
    }

    @Override
    public CompletableFuture<DequeueResult> dequeueAsync(String group, String topic, int queueId, int num) {
        return null;
    }

    @Override
    public DequeueResult dequeue(String group, String topic, int queueId, int num) {
        return null;
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset) {

        return null;
    }

    @Override
    public DequeueResult get(String topic, int queueId, long offset, int num) {

        return null;
    }

    @Override
    public DequeueResult get(GetRequest request) {

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
