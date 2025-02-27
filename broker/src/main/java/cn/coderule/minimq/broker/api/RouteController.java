package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.broker.server.model.RequestContext;
import cn.coderule.minimq.domain.model.MessageQueue;
import java.util.Set;

public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    public Set<MessageQueue> getRoute(RequestContext context, String topic) {
        return routeService.getRoute(context, topic);
    }

    public Set<MessageQueue> getOrCreateRoute(RequestContext context, String topic) {
        return routeService.getOrCreateRoute(context, topic);
    }
}
