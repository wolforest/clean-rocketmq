package cn.coderule.minimq.broker.domain.route;

import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.domain.model.Topic;
import java.util.LinkedHashSet;
import java.util.Set;

public class RouteMocker {
    private final TopicService topicService;

    public RouteMocker(TopicService topicService) {
        this.topicService = topicService;
    }

    public Set<MessageQueue> getRoute(String topicName) {
        Set<MessageQueue> result = new LinkedHashSet<>();
        Topic topic = topicService.get(topicName);
        if (topic == null) {
            return result;
        }

        return toRoute(topic);
    }

    public Set<MessageQueue> toRoute(Topic topic) {
        return null;
    }
}
