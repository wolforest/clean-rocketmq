package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.rpc.common.rpc.RpcClient;
import cn.coderule.minimq.rpc.store.StoreClient;
import lombok.Setter;

@Setter
public class TopicClient extends AbstractStoreClient implements StoreClient, TopicStore {

    public TopicClient(RpcClient rpcClient, String address) {
        super(rpcClient, address);
    }

    @Override
    public boolean exists(String topicName) {


        return false;
    }

    @Override
    public Topic getTopic(String topicName) {


        return null;
    }

    @Override
    public void saveTopic(Topic topic) {

    }

    @Override
    public void deleteTopic(String topicName) {

    }

}
