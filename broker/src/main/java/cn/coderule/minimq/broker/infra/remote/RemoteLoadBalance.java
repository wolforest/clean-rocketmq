package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.rpc.registry.route.RouteLoader;
import cn.coderule.minimq.domain.config.server.BrokerConfig;

public class RemoteLoadBalance {
    private final BrokerConfig brokerConfig;
    private final RouteLoader routeLoader;

    public RemoteLoadBalance(BrokerConfig brokerConfig, RouteLoader routeLoader) {
        this.brokerConfig = brokerConfig;
        this.routeLoader = routeLoader;
    }

    public String findByTopic(String topicName) {
        return null;
    }

    public String findByGroup(String groupName, int groupNo) {
        return null;
    }

    public String findByStoreGroup(String storeGroup) {
        return null;
    }
}
