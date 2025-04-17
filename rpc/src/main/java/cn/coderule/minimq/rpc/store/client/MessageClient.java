package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.dto.GetRequest;
import cn.coderule.minimq.domain.domain.dto.GetResult;
import cn.coderule.minimq.domain.service.store.api.MessageStore;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Setter;

@Setter
public class MessageClient extends AbstractStoreClient implements StoreClient, MessageStore {

    public MessageClient(RpcClient rpcClient, String address) {
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
    public GetResult get(String topic, int queueId, long offset) {

        return null;
    }

    @Override
    public GetResult get(String topic, int queueId, long offset, int num) {

        return null;
    }

    @Override
    public GetResult get(GetRequest request) {

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
