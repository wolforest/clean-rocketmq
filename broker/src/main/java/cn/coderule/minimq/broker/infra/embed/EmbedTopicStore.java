package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;

public class EmbedTopicStore implements TopicStore {

    public EmbedTopicStore(EmbedLoadBalance loadBalance) {
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
