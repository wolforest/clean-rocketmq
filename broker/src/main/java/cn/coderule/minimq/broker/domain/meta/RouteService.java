package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.broker.server.model.RequestContext;
import cn.coderule.minimq.domain.config.TopicConfig;
import cn.coderule.minimq.domain.model.MessageQueue;
import cn.coderule.minimq.domain.model.Topic;
import java.util.Set;

/**
 * load route info from name server
 *
 */
public class RouteService {
    private final TopicConfig topicConfig;
    private final TopicService topicService;
    private final RouteMocker routeMocker;
    // private RegistryClient registryClient;

    public RouteService(TopicConfig topicConfig, TopicService topicService) {
        this.topicConfig = topicConfig;
        this.topicService = topicService;
        this.routeMocker = new RouteMocker(topicService);
    }

    public Set<MessageQueue> getRoute(RequestContext context, String topic) {
        return routeMocker.getRoute(topic);
    }

    public Set<MessageQueue> register(Topic topic) {
        return routeMocker.toRoute(topic);
    }
}
