package cn.coderule.minimq.broker.api;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.domain.route.RouteMocker;
import cn.coderule.minimq.broker.domain.route.RouteService;
import cn.coderule.minimq.broker.domain.route.TopicService;
import cn.coderule.minimq.broker.server.bootstrap.RequestContext;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.domain.model.Topic;
import java.util.Set;

public class RouteController {
    private final RouteService routeService;
    private final TopicService topicService;
    private final RouteMocker routeMocker;

    public RouteController(RouteService routeService, TopicService topicService) {
        this.routeService = routeService;
        this.topicService = topicService;
        routeMocker = new RouteMocker(topicService);
    }

    public Set<MessageQueue> getRoute(RequestContext context, String topicName) {
        return routeService.get(context, topicName);
    }

    public Set<MessageQueue> getOrCreateRoute(RequestContext context, String topicName) {
        Set<MessageQueue> result = routeService.get(context, topicName);
        if (CollectionUtil.notEmpty(result)) {
            return result;
        }

        Topic topic = topicService.getOrCreate(topicName);
        return routeMocker.toRoute(topic);
    }
}
