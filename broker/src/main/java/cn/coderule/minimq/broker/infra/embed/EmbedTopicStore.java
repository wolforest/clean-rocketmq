package cn.coderule.minimq.broker.infra.embed;

import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.meta.topic.TopicRequest;
import cn.coderule.minimq.domain.service.broker.infra.meta.TopicFacade;
import java.util.concurrent.CompletableFuture;

public class EmbedTopicStore implements TopicFacade {

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
    public void saveTopic(TopicRequest request) {

    }

    @Override
    public void deleteTopic(TopicRequest request) {

    }

    @Override
    public CompletableFuture<Topic> getTopicAsync(String topicName) {
        return null;
    }


}
