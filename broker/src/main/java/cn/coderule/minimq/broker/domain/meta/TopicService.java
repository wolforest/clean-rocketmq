package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.domain.model.Topic;

/**
 * topic service
 *  - create/get/save topic
 *  - support local/remote mode
 */
public class TopicService {
    private cn.coderule.minimq.domain.service.store.domain.meta.TopicService topicService;

    public boolean exists(String topicName) {
        return topicService.exists(topicName);
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
        return topicService.getTopic(topicName);
    }

    public void save(Topic topicName) {
        topicService.putTopic(topicName);
    }

    public void delete(String topicName) {
        topicService.deleteTopic(topicName);
    }

}
