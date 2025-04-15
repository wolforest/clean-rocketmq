package cn.coderule.minimq.broker.domain.route;

import cn.coderule.minimq.rpc.registry.protocol.route.PublishInfo;
import cn.coderule.minimq.broker.infra.route.RouteLoader;
import cn.coderule.minimq.broker.server.bootstrap.RequestContext;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import java.util.Set;

/**
 * load route info from name server
 *
 */
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

    public PublishInfo getPublishInfo(String topicName) {
        return routeLoader.getPublishInfo(topicName);
    }

    public Set<MessageQueue> getSubscriptionInfo(String topicName) {
        return routeLoader.getSubscriptionInfo(topicName);
    }

    public String getAddressInPublish(String groupName) {
        return routeLoader.getAddressInPublish(groupName);
    }

    public String getAddressInSubscription(String groupName, long groupNo, boolean inGroup) {
        return routeLoader.getAddressInSubscription(groupName, groupNo, inGroup);
    }


}
