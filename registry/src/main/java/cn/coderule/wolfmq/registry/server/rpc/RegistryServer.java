package cn.coderule.wolfmq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.config.server.RegistryConfig;
import cn.coderule.wolfmq.registry.processor.ClusterProcessor;
import cn.coderule.wolfmq.registry.processor.KVProcessor;
import cn.coderule.wolfmq.registry.processor.PropertyProcessor;
import cn.coderule.wolfmq.registry.processor.RegistryProcessor;
import cn.coderule.wolfmq.registry.processor.RouteProcessor;
import cn.coderule.wolfmq.registry.processor.TopicProcessor;
import cn.coderule.wolfmq.registry.server.bootstrap.RegistryContext;
import cn.coderule.wolfmq.rpc.common.rpc.RpcServer;
import cn.coderule.wolfmq.domain.config.network.RpcServerConfig;
import cn.coderule.wolfmq.rpc.common.rpc.netty.NettyServer;
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
