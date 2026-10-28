package com.wolfmq.broker.domain.meta;

import com.wolfmq.domain.domain.message.enums.MessageType;
import com.wolfmq.domain.domain.meta.topic.Topic;
import com.wolfmq.domain.domain.meta.topic.TopicRequest;
import com.wolfmq.rpc.store.facade.TopicFacade;
import java.util.concurrent.CompletableFuture;

/**
 * topic service
 *  - create/get/save topic
 *  - support local/remote mode
 */
public class BrokerTopicService implements TopicFacade {
    private final TopicFacade store;

    public BrokerTopicService(TopicFacade store) {
        this.store = store;
    }

    @Override
    public boolean exists(String topicName) {
        return store.exists(topicName);
    }

    @Override
    public CompletableFuture<Topic> getTopicAsync(String topicName) {
        return store.getTopicAsync(topicName);
    }

    @Override
    public Topic getTopic(String topicName) {
        return store.getTopic(topicName);
    }

    @Override
    public void saveTopic(TopicRequest request) {
        store.saveTopic(request);
    }

    @Override
    public void deleteTopic(TopicRequest request) {
        store.deleteTopic(request);
    }

    public MessageType getTopicType(String topicName) {
        Topic topic = getTopic(topicName);
        if (topic == null) {
            return MessageType.UNKNOWN;
        }

        return topic.getTopicType();
    }

}
