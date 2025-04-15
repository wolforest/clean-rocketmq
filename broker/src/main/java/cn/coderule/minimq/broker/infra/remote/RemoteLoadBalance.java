package cn.coderule.minimq.broker.infra.remote;

import cn.coderule.minimq.broker.infra.route.RouteLoader;
import cn.coderule.minimq.domain.config.BrokerConfig;

public class RemoteLoadBalance {
    private final BrokerConfig brokerConfig;
    private final RouteLoader routeLoader;

    public RemoteLoadBalance(BrokerConfig brokerConfig, RouteLoader routeLoader) {
        this.brokerConfig = brokerConfig;
        this.routeLoader = routeLoader;
    }

    public String getServerAddress(String topicName) {
        return null;
    }

    public String getServerAddress(String topicName, int queueId) {
        return null;
    }

}
