package cn.coderule.wolfmq.broker.infra.remote;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionRequest;
import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import cn.coderule.wolfmq.rpc.store.facade.SubscriptionFacade;
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
