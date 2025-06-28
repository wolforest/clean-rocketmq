package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.domain.core.enums.message.MessageType;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.domain.service.broker.infra.meta.TopicFacade;
import java.util.concurrent.CompletableFuture;

/**
 * topic service
 *  - create/get/save topic
 *  - support local/remote mode
 */
public class TopicService implements TopicFacade {
    private final TopicFacade store;

    public TopicService(TopicFacade store) {
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
