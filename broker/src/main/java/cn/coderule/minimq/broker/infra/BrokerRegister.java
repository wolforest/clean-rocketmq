package cn.coderule.minimq.broker.infra;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.rpc.rpc.config.RpcClientConfig;
import cn.coderule.minimq.rpc.registry.RegistryClient;
import cn.coderule.minimq.rpc.registry.client.DefaultRegistryClient;
import cn.coderule.minimq.rpc.registry.protocol.cluster.BrokerInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.HeartBeat;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ServerInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrokerRegister implements Lifecycle {
    private final BrokerConfig brokerConfig;
    @Getter
    private final RegistryClient registryClient;

    public BrokerRegister(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.registryClient = new DefaultRegistryClient(
            new RpcClientConfig(),
            brokerConfig.getRegistryAddress()
        );
    }

    @Override
    public void start() {
        registryClient.start();
    }

    @Override
    public void shutdown() {
        registryClient.shutdown();
    }

    public void register() {
        registryClient.registerBroker(new BrokerInfo());
    }

    public void unregister() {
        registryClient.unregisterBroker(new ServerInfo());
    }

    public void heartbeat() {
        registryClient.brokerHeartbeat(new HeartBeat());
    }

    public RouteInfo syncRouteInfo(String topicName) throws Exception {
        return registryClient.syncRouteInfo(topicName, brokerConfig.getSyncRouteTimeout());
    }

    public RouteInfo syncRouteInfo(String topicName, long timeout) throws Exception {
        return registryClient.syncRouteInfo(topicName, timeout);
    }
}
