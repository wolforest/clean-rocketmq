package cn.coderule.minimq.broker.domain.route;

import cn.coderule.minimq.rpc.registry.route.RouteLoader;
import cn.coderule.minimq.broker.server.bootstrap.RequestContext;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * load route info from name server
 *
 */
@Slf4j
public class RouteService {
    private final RouteMocker routeMocker;
    private final RouteLoader routeLoader;    // private RegistryClient registryClient;

    public RouteService(RouteLoader routeLoader, RouteMocker routeMocker) {
        this.routeLoader = routeLoader;
        this.routeMocker = routeMocker;
    }

    public Set<MessageQueue> get(RequestContext context, String topic) {
        return routeMocker.getRoute(topic);
    }


}
