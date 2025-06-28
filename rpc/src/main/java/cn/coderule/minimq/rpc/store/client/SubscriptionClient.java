package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.broker.infra.meta.SubscriptionStore;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import java.util.concurrent.CompletableFuture;

public class SubscriptionClient extends AbstractStoreClient implements StoreClient, SubscriptionStore {

    public SubscriptionClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
    }

    @Override
    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        return null;
    }
}
