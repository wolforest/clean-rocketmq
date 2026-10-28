package com.wolfmq.broker.infra.remote;

import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.meta.topic.Topic;
import com.wolfmq.domain.domain.meta.topic.TopicRequest;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.facade.TopicFacade;
import java.util.concurrent.CompletableFuture;

public class RemoteTopicStore extends AbstractRemoteStore implements TopicFacade {
    private final BrokerConfig brokerConfig;
    private final RpcClient rpcClient;

    public RemoteTopicStore(BrokerConfig brokerConfig, RemoteLoadBalance loadBalance, RpcClient rpcClient) {
        super(loadBalance);

        this.brokerConfig = brokerConfig;
        this.rpcClient = rpcClient;
    }

    @Override
    public boolean exists(String topicName) {
        return false;
    }

    @Override
    public CompletableFuture<Topic> getTopicAsync(String topicName) {
        return null;
    }

    @Override
    public Topic getTopic(String topicName) {
        return null;
    }

    @Override
    public void saveTopic(TopicRequest request) {

    }

    @Override
    public void deleteTopic(TopicRequest request) {

    }
}
