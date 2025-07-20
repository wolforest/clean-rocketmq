package cn.coderule.minimq.rpc.store.facade;

import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.domain.meta.topic.TopicRequest;
import java.util.concurrent.CompletableFuture;

public interface TopicFacade {
    boolean exists(String topicName);

    CompletableFuture<Topic> getTopicAsync(String topicName);
    Topic getTopic(String topicName);

    void saveTopic(TopicRequest request);
    void deleteTopic(TopicRequest request);
}
