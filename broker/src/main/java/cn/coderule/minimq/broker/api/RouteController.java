package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.domain.model.MessageQueue;
import java.util.Set;

public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    public Set<MessageQueue> getRoute(String topic) {
        return routeService.getRoute(topic);
    }

    public Set<MessageQueue> getOrCreateRoute(String topic) {
        return routeService.getOrCreateRoute(topic);
    }
}
