package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.domain.domain.enums.message.MessageType;
import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.domain.service.broker.infra.TopicStore;
import java.util.concurrent.CompletableFuture;

/**
 * topic service
 *  - create/get/save topic
 *  - support local/remote mode
 */
public class TopicService implements TopicStore {
    private final TopicStore store;

    public TopicService(TopicStore store) {
        this.store = store;
    }

    @Override
    public CompletableFuture<Topic> getTopicAsync(String topicName) {
        return store.getTopicAsync(topicName);
    }

    @Override
    public Topic getTopic(String topicName) {
        return store.getTopic(topicName);
    }

    public MessageType getTopicType(String topicName) {
        Topic topic = getTopic(topicName);
        if (topic == null) {
            return MessageType.UNKNOWN;
        }

        return topic.getTopicType();
    }

}
