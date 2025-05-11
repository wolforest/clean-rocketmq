package cn.coderule.minimq.domain.service.broker.infra;

import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import java.util.concurrent.CompletableFuture;

public interface TopicStore {
    CompletableFuture<Topic> getTopicAsync(String topicName);
    Topic getTopic(String topicName);
}
