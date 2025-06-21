package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.common.util.net.Address;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.exception.InvalidConfigException;
import cn.coderule.minimq.domain.domain.model.cluster.selector.MessageQueueView;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.GroupInfo;
import cn.coderule.minimq.domain.domain.model.cluster.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.route.RouteLoader;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * load route info from name server
 *
 */
@Slf4j
public class RouteService {
    private final BrokerConfig brokerConfig;
    private final RouteMocker routeMocker;
    private final RouteLoader routeLoader;

    public RouteService(BrokerConfig brokerConfig, RouteLoader routeLoader, RouteMocker routeMocker) {
        this.brokerConfig = brokerConfig;
        this.routeLoader = routeLoader;
        this.routeMocker = routeMocker;

        if (routeLoader == null && routeMocker == null) {
            throw new InvalidConfigException("invalid config: registryAddress and enableEmbedStore");
        }
    }

    public MessageQueueView getQueueView(RequestContext context, String topic) {
        RouteInfo routeInfo = get(context, topic);
        if (routeInfo == null) {
            return null;
        }

        return new MessageQueueView(topic, routeInfo, null);
    }

    public RouteInfo get(RequestContext context, String topic) {
        return get(context, topic, List.of());
    }

    public RouteInfo get(RequestContext context, String topic, List<Address> addressList) {
        RouteInfo routeInfo;
        if (routeLoader != null) {
            routeInfo = routeLoader.getRoute(topic);
        } else {
            routeInfo = routeMocker.getRoute(topic);
        }

        return formatRouteAddress(context, routeInfo, addressList);
    }

    private RouteInfo formatRouteAddress(RequestContext context, RouteInfo routeInfo, List<Address> addressList) {
        if (routeInfo == null || CollectionUtil.isEmpty(addressList)) {
            return routeInfo;
        }

        if (brokerConfig.isEnableEmbedStore()) {
            return formatEmbedRouteAddress(context, routeInfo, addressList);
        }

        return formatRemoteRouteAddress(routeInfo, addressList);
    }

    /**
     * in embed mode, replace the address port
     */
    private RouteInfo formatEmbedRouteAddress(RequestContext context, RouteInfo routeInfo, List<Address> addressList) {
        if (null == context.getServerPort()) {
            return routeInfo;
        }

        for (GroupInfo groupInfo : routeInfo.getBrokerDatas()) {
            for (Map.Entry<Long, String> entry : groupInfo.getBrokerAddrs().entrySet()) {
                String address = NetworkUtil.replacePort(entry.getValue(), context.getServerPort());
                entry.setValue(address);
            }
        }

        return routeInfo;
    }

    /**
     * in remote mode, replace the address by addressList
     */
    private RouteInfo formatRemoteRouteAddress(@NonNull RouteInfo routeInfo, @NonNull List<Address> addressList) {
        String address = getAddressString(addressList);
        for (GroupInfo groupInfo : routeInfo.getBrokerDatas()) {
            for (Map.Entry<Long, String> entry : groupInfo.getBrokerAddrs().entrySet()) {
                entry.setValue(address);
            }
        }

        return routeInfo;
    }

    private String getAddressString(List<Address> addressList) {
        StringBuilder brokerAddress = new StringBuilder();
        for (Address address : addressList) {
            brokerAddress.append(address.getHostAndPort()).append(";");
        }
        return brokerAddress.toString();
    }


}
