package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.domain.service.broker.infra.TopicStore;
import java.util.concurrent.CompletableFuture;

public class EmbedTopicStore implements TopicStore, cn.coderule.minimq.domain.service.store.api.TopicStore {

    public EmbedTopicStore(EmbedLoadBalance loadBalance) {
    }

    public boolean exists(String topicName) {
        return false;
    }

    @Override
    public Topic getTopic(String topicName) {
        return null;
    }

    @Override
    public CompletableFuture<Topic> getTopicAsync(String topicName) {
        return null;
    }

    @Override
    public void saveTopic(Topic topic) {

    }

    @Override
    public void deleteTopic(String topicName) {

    }

}
