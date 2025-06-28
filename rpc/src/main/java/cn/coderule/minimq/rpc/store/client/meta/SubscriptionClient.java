package cn.coderule.minimq.rpc.store.client.meta;

import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.service.broker.infra.meta.SubscriptionFacade;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import cn.coderule.minimq.rpc.store.client.AbstractStoreClient;
import java.util.concurrent.CompletableFuture;

public class SubscriptionClient extends AbstractStoreClient implements StoreClient, SubscriptionFacade {

    public SubscriptionClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
    }

    @Override
    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        return null;
    }
}
