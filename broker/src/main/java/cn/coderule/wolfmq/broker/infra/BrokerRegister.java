package cn.coderule.wolfmq.broker.infra;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.rpc.common.rpc.netty.NettyClient;
import cn.coderule.wolfmq.rpc.registry.RegistryClient;
import cn.coderule.wolfmq.rpc.registry.client.DefaultRegistryClient;
import cn.coderule.wolfmq.domain.domain.cluster.server.BrokerInfo;
import cn.coderule.wolfmq.domain.domain.cluster.server.HeartBeat;
import cn.coderule.wolfmq.domain.domain.cluster.server.ServerInfo;
import cn.coderule.wolfmq.domain.domain.cluster.route.RouteInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BrokerRegister implements Lifecycle {
    private final BrokerConfig brokerConfig;
    @Getter
    private final RegistryClient registryClient;

    public BrokerRegister(BrokerConfig brokerConfig, NettyClient nettyClient) {
        this.brokerConfig = brokerConfig;

        this.registryClient = new DefaultRegistryClient(
            brokerConfig.getRpcClientConfig(),
            brokerConfig.getRegistryAddress(),
            nettyClient);
    }

    @Override
    public void start() throws Exception {
        registryClient.start();
    }

    @Override
    public void shutdown() throws Exception {
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
