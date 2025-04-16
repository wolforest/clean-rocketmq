package cn.coderule.minimq.broker.domain.route;

import cn.coderule.minimq.domain.domain.exception.InvalidConfigException;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.route.RouteLoader;
import cn.coderule.minimq.broker.server.bootstrap.RequestContext;
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

        if (routeLoader == null && routeMocker == null) {
            throw new InvalidConfigException("invalid config: registryAddress and enableEmbedStore");
        }
    }

    public RouteInfo get(RequestContext context, String topic) {
        if (routeLoader != null) {
            return routeLoader.getRoute(topic);
        }

        return routeMocker.getRoute(topic);
    }


}
