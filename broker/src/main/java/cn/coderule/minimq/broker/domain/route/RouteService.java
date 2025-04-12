package cn.coderule.minimq.broker.domain.route;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.broker.domain.route.model.PublishInfo;
import cn.coderule.minimq.broker.domain.route.model.RouteCache;
import cn.coderule.minimq.broker.server.bootstrap.RequestContext;
import cn.coderule.minimq.domain.config.TopicConfig;
import cn.coderule.minimq.domain.domain.constant.MQConstants;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import java.util.Set;

/**
 * load route info from name server
 *
 */
public class RouteService {
    private final RouteMocker routeMocker;
    private final RouteCache routeCache;
    // private RegistryClient registryClient;

    public RouteService(RouteCache routeCache, RouteMocker routeMocker) {
        this.routeCache = routeCache;
        this.routeMocker = routeMocker;
    }

    public Set<MessageQueue> get(RequestContext context, String topic) {
        return routeMocker.getRoute(topic);
    }




}
