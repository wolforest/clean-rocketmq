package com.wolfmq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.rpc.common.rpc.RpcProcessor;
import com.wolfmq.rpc.common.rpc.RpcServer;
import com.wolfmq.domain.config.network.RpcServerConfig;
import com.wolfmq.rpc.common.rpc.netty.NettyServer;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryServer implements Lifecycle {
    private final RegistryConfig registryConfig;

    private final RpcServer rpcServer;
    private final List<RpcProcessor> processors;

    public RegistryServer(
        RegistryConfig registryConfig,
        RpcServerConfig serverConfig,
        ConnectionManager connectionManager,
        List<RpcProcessor> processors
    ) {
        this.registryConfig = registryConfig;

        this.rpcServer = new NettyServer(serverConfig, connectionManager);
        this.processors = processors;
    }

    @Override
    public void initialize() throws Exception {
        registerProcessor();
    }

    @Override
    public void start() throws Exception {
        rpcServer.start();
    }

    @Override
    public void shutdown() throws Exception {
        rpcServer.shutdown();
    }

    private void registerProcessor() {
        processors.forEach(rpcServer::registerProcessor);
    }
}
