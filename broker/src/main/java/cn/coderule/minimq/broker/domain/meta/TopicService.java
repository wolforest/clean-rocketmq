package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicStore;

/**
 * topic service
 *  - create/get/save topic
 *  - support local/remote mode
 */
public class TopicService {
    private TopicStore topicStore;

    public boolean exists(String topicName) {
        return topicStore.exists(topicName);
    }

    public Topic getOrCreate(String topicName) {
        Topic oldTopic = get(topicName);
        if (null != oldTopic) {
            return oldTopic;
        }

        Topic newTopic = Topic.builder()
            .topicName(topicName)
            .build();

        save(newTopic);
        return newTopic;
    }

    public Topic get(String topicName) {
        return topicStore.getTopic(topicName);
    }

    public void save(Topic topicName) {
        topicStore.putTopic(topicName);
    }

    public void delete(String topicName) {
        topicStore.deleteTopic(topicName);
    }

}
