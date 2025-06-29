package cn.coderule.minimq.rpc.store.client.meta;

import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.meta.topic.TopicRequest;
import cn.coderule.minimq.domain.service.broker.infra.meta.TopicFacade;
import cn.coderule.minimq.domain.service.store.api.meta.TopicStore;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import cn.coderule.minimq.rpc.store.client.AbstractStoreClient;
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
