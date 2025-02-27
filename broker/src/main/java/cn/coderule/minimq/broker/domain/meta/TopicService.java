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

    public boolean exists(String topic) {
        return topicStore.exists(topic);
    }

    public Topic getOrCreate(String topic) {
        Topic oldTopic = get(topic);
        if (null != oldTopic) {
            return oldTopic;
        }

        Topic newTopic = Topic.builder()
            .topicName(topic)
            .build();

        save(newTopic);
        return newTopic;
    }

    public Topic get(String topic) {
        return topicStore.getTopic(topic);
    }

    public void save(Topic topic) {
        topicStore.putTopic(topic);
    }

    public void delete(String topic) {
        topicStore.deleteTopic(topic);
    }

}
