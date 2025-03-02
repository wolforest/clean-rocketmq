package cn.coderule.minimq.store.api;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;

public class TopicStoreImpl implements TopicStore {
    private final cn.coderule.minimq.domain.service.store.domain.meta.TopicStore topicStore;

    public TopicStoreImpl(cn.coderule.minimq.domain.service.store.domain.meta.TopicStore topicStore) {
        this.topicStore = topicStore;
    }

    @Override
    public Topic getTopic(String topicName) {
        return topicStore.getTopic(topicName);
    }

    @Override
    public void putTopic(Topic topic) {
        topicStore.putTopic(topic);
    }

    @Override
    public void deleteTopic(String topicName) {
        topicStore.deleteTopic(topicName);
    }
}
