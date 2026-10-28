package com.wolfmq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.domain.store.service.ChannelCloser;
import com.wolfmq.registry.server.bootstrap.RegistryContext;
import com.wolfmq.domain.config.network.RpcServerConfig;

public class RpcManager implements Lifecycle {
    private RegistryServer server;

    @Override
    public void initialize() throws Exception {
        ChannelCloser channelCloser = RegistryContext.getBean(ChannelCloser.class);
        ConnectionManager connectionManager = new ConnectionManager(channelCloser);

        RegistryConfig registryConfig = RegistryContext.getBean(RegistryConfig.class);
        RpcServerConfig serverConfig = RegistryContext.getBean(RpcServerConfig.class);
        server = new RegistryServer(registryConfig, serverConfig, connectionManager);

        server.initialize();
    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void shutdown() throws Exception {
        server.shutdown();
    }

}
