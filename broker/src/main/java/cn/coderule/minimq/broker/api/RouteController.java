package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.domain.route.RouteService;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.concurrent.CompletableFuture;

public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    public CompletableFuture<RouteInfo> getRoute(RequestContext context, String topicName) {
        RouteInfo routeInfo = routeService.get(context, topicName);
        return CompletableFuture.completedFuture(routeInfo);
    }

    private void validateTopic(String topicName) {

    }

}
