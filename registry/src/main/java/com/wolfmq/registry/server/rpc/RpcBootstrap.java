package com.wolfmq.registry.server.rpc;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.network.RpcServerConfig;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.registry.domain.store.service.ChannelCloser;
import com.wolfmq.registry.processor.ClusterProcessor;
import com.wolfmq.registry.processor.KVProcessor;
import com.wolfmq.registry.processor.PropertyProcessor;
import com.wolfmq.registry.processor.RegistryProcessor;
import com.wolfmq.registry.processor.RouteProcessor;
import com.wolfmq.registry.processor.TopicProcessor;
import com.wolfmq.rpc.common.rpc.RpcProcessor;
import java.util.List;

public class RpcBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public RpcBootstrap(ApplicationContext context) {
        this.context = context;
    }

    private RegistryServer server;

    @Override
    public void initialize() throws Exception {
        ChannelCloser channelCloser = context.getBean(ChannelCloser.class, ContextNamespace.APP);
        ConnectionManager connectionManager = new ConnectionManager(channelCloser);

        RegistryConfig registryConfig = context.getBean(RegistryConfig.class, ContextNamespace.APP);
        RpcServerConfig serverConfig = context.getBean(RpcServerConfig.class, ContextNamespace.APP);

        List<RpcProcessor> processors = List.of(
            context.getBean(KVProcessor.class, ContextNamespace.APP),
            context.getBean(PropertyProcessor.class, ContextNamespace.APP),
            context.getBean(ClusterProcessor.class, ContextNamespace.APP),
            context.getBean(RegistryProcessor.class, ContextNamespace.APP),
            context.getBean(RouteProcessor.class, ContextNamespace.APP),
            context.getBean(TopicProcessor.class, ContextNamespace.APP)
        );

        server = new RegistryServer(registryConfig, serverConfig, connectionManager, processors);

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
