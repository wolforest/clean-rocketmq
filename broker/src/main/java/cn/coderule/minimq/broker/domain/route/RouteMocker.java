package cn.coderule.minimq.broker.domain.route;

import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import java.util.LinkedHashSet;
import java.util.Set;

public class RouteMocker {
    private final TopicStore topicStore;

    public RouteMocker(TopicStore topicStore) {
        this.topicStore = topicStore;
    }

    public Set<MessageQueue> getRoute(String topicName) {
        Set<MessageQueue> result = new LinkedHashSet<>();
        Topic topic = topicStore.getTopic(topicName);
        if (topic == null) {
            return result;
        }

        return toRoute(topic);
    }

    public Set<MessageQueue> toRoute(Topic topic) {
        return null;
    }
}
