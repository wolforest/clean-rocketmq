package cn.coderule.minimq.domain.service.broker.infra;

import cn.coderule.minimq.domain.domain.model.Topic;
import java.util.concurrent.CompletableFuture;

public interface TopicStore {
    CompletableFuture<Topic> getTopic(String topicName);
}
