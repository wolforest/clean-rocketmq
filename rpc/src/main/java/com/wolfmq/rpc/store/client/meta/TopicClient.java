package com.wolfmq.rpc.store.client.meta;

import com.wolfmq.domain.domain.meta.topic.Topic;
import com.wolfmq.domain.domain.meta.topic.TopicRequest;
import com.wolfmq.rpc.common.rpc.RpcClient;
import com.wolfmq.rpc.store.StoreClient;
import com.wolfmq.rpc.store.client.AbstractStoreClient;
import com.wolfmq.rpc.store.facade.TopicFacade;
import java.util.concurrent.CompletableFuture;
import lombok.Setter;

@Setter
public class TopicClient extends AbstractStoreClient implements StoreClient, TopicFacade {

    public TopicClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
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
