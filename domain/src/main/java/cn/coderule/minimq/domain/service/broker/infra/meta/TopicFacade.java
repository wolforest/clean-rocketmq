package cn.coderule.minimq.domain.service.broker.infra.meta;

import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import java.util.concurrent.CompletableFuture;

public interface TopicFacade {
    CompletableFuture<Topic> getTopicAsync(String topicName);
    Topic getTopic(String topicName);
}
