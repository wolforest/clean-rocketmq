package com.wolfmq.broker.infra;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.rpc.common.rpc.netty.NettyClient;
import com.wolfmq.rpc.registry.RegistryClient;
import com.wolfmq.rpc.registry.client.DefaultRegistryClient;
import com.wolfmq.domain.server.registry.BrokerInfo;
import com.wolfmq.domain.server.heartbeat.HeartBeat;
import com.wolfmq.domain.server.registry.ServerInfo;
import com.wolfmq.domain.server.route.RouteInfo;
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
