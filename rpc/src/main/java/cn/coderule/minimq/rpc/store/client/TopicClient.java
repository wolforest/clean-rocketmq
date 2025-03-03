package cn.coderule.minimq.rpc.store.client;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.rpc.store.StoreClient;
import lombok.Setter;

@Setter
public class TopicClient extends AbstractStoreClient implements StoreClient, TopicStore {
    private TopicStore localTopicStore;

    @Override
    public boolean exists(String topicName) {
        if (isLocal(topicName)) {
            return localTopicStore.exists(topicName);
        }

        return false;
    }

    @Override
    public Topic getTopic(String topicName) {
        if (isLocal(topicName)) {
            return localTopicStore.getTopic(topicName);
        }

        return null;
    }

    @Override
    public void putTopic(Topic topic) {
        if (isLocal(topic.getTopicName())) {
            localTopicStore.putTopic(topic);
            return;
        }
    }

    @Override
    public void deleteTopic(String topicName) {
        if (isLocal(topicName)) {
            localTopicStore.deleteTopic(topicName);
            return;
        }
    }

}
