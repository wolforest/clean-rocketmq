package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;

/**
 * topic service
 *  - create/get/save topic
 *  - support local/remote mode
 */
public class TopicService {
    private TopicStore topicClient;

    public boolean exists(String topicName) {
        return topicClient.exists(topicName);
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
        return topicClient.getTopic(topicName);
    }

    public void save(Topic topicName) {
        topicClient.saveTopic(topicName);
    }

    public void delete(String topicName) {
        topicClient.deleteTopic(topicName);
    }

}
