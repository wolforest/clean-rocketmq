package com.wolfmq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.processor.ClusterProcessor;
import com.wolfmq.registry.processor.KVProcessor;
import com.wolfmq.registry.processor.PropertyProcessor;
import com.wolfmq.registry.processor.RegistryProcessor;
import com.wolfmq.registry.processor.RouteProcessor;
import com.wolfmq.registry.processor.TopicProcessor;
import com.wolfmq.registry.server.bootstrap.RegistryContext;
import com.wolfmq.rpc.common.rpc.RpcServer;
import com.wolfmq.domain.config.network.RpcServerConfig;
import com.wolfmq.rpc.common.rpc.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryServer implements Lifecycle {
    private final RegistryConfig registryConfig;

    private final RpcServer rpcServer;

    public RegistryServer(RegistryConfig registryConfig, RpcServerConfig serverConfig, ConnectionManager connectionManager) {
        this.registryConfig = registryConfig;

        this.rpcServer = new NettyServer(serverConfig, connectionManager);
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
        rpcServer.registerProcessor(RegistryContext.getBean(KVProcessor.class));
        rpcServer.registerProcessor(RegistryContext.getBean(PropertyProcessor.class));
        rpcServer.registerProcessor(RegistryContext.getBean(ClusterProcessor.class));
        rpcServer.registerProcessor(RegistryContext.getBean(RegistryProcessor.class));
        rpcServer.registerProcessor(RegistryContext.getBean(RouteProcessor.class));
        rpcServer.registerProcessor(RegistryContext.getBean(TopicProcessor.class));
    }
}
