package com.wolfmq.broker.infra.remote;

import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import com.wolfmq.domain.domain.meta.subscription.SubscriptionRequest;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.facade.SubscriptionFacade;
import java.util.concurrent.CompletableFuture;

public class RemoteSubscriptionStore extends AbstractRemoteStore implements SubscriptionFacade {
    private final BrokerConfig brokerConfig;
    private final RpcClient rpcClient;

    public RemoteSubscriptionStore(BrokerConfig brokerConfig, RemoteLoadBalance loadBalance, RpcClient rpcClient) {
        super(loadBalance);

        this.brokerConfig = brokerConfig;
        this.rpcClient = rpcClient;
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
