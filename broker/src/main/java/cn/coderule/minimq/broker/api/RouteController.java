package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.domain.route.RouteService;
import cn.coderule.minimq.broker.server.bootstrap.RequestContext;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import java.util.Set;

public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    public Set<MessageQueue> getRoute(RequestContext context, String topicName) {
        return routeService.get(context, topicName);
    }

}
