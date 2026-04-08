package cn.coderule.wolfmq.rpc.store.client.meta;

import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionRequest;
import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import cn.coderule.wolfmq.rpc.store.StoreClient;
import cn.coderule.wolfmq.rpc.store.client.AbstractStoreClient;
import cn.coderule.wolfmq.rpc.store.facade.SubscriptionFacade;
import java.util.concurrent.CompletableFuture;

public class SubscriptionClient extends AbstractStoreClient implements StoreClient, SubscriptionFacade {

    public SubscriptionClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
    }

    @Override
    public boolean existsGroup(String topicName, String groupName) {
        return false;
    }

    @Override
    public SubscriptionGroup getGroup(String topicName, String groupName) {
        return null;
    }

    @Override
    public CompletableFuture<SubscriptionGroup> getGroupAsync(String topicName, String groupName) {
        return null;
    }

    @Override
    public void putGroup(SubscriptionRequest request) {

    }

    @Override
    public void saveGroup(SubscriptionRequest request) {

    }

    @Override
    public void deleteGroup(SubscriptionRequest request) {

    }
}
