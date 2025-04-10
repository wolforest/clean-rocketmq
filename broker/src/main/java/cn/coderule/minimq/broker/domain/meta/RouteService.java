package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.broker.server.bootstrap.RequestContext;
import cn.coderule.minimq.domain.config.TopicConfig;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import java.util.Set;

/**
 * load route info from name server
 *
 */
public class RouteService {
    private final TopicConfig topicConfig;
    private final RouteMocker routeMocker;
    // private RegistryClient registryClient;

    public RouteService(TopicConfig topicConfig, RouteMocker routeMocker) {
        this.topicConfig = topicConfig;
        this.routeMocker = routeMocker;
    }

    public Set<MessageQueue> get(RequestContext context, String topic) {
        return routeMocker.getRoute(topic);
    }

}
