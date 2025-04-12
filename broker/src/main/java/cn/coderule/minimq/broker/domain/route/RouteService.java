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

    public PublishInfo getPublishInfo(String topicName) {
        return null;
    }

    public Set<MessageQueue> getSubscriptionInfo(String topicName) {
        return null;
    }

    public String getAddressInPublish(String groupName) {
        return routeCache.getAddress(groupName, MQConstants.MASTER_ID);
    }

    public String getAddressInSubscription(String groupName, long groupNo, boolean inGroup) {
        String address = routeCache.getAddress(groupName, groupNo);

        boolean isSlave = groupNo != MQConstants.MASTER_ID;
        if (StringUtil.isBlank(address) && isSlave) {
            address = routeCache.getAddress(groupName, groupNo + 1);
        }

        if (StringUtil.isBlank(address) && inGroup) {
            address = routeCache.getFirstAddress(groupName);
        }

        return address;
    }
}
