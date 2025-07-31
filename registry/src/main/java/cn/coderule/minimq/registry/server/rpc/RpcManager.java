package cn.coderule.minimq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.RegistryConfig;
import cn.coderule.minimq.registry.domain.store.service.ChannelCloser;
import cn.coderule.minimq.registry.server.bootstrap.RegistryContext;
import cn.coderule.minimq.domain.config.network.RpcServerConfig;

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
