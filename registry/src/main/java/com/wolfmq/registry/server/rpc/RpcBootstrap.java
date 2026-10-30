package com.wolfmq.registry.server.rpc;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.network.RpcServerConfig;
import com.wolfmq.domain.config.server.RegistryConfig;
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
        ChannelCloser channelCloser = context.getBean(ChannelCloser.class);
        ConnectionManager connectionManager = new ConnectionManager(channelCloser);

        RegistryConfig registryConfig = context.getBean(RegistryConfig.class);
        RpcServerConfig serverConfig = context.getBean(RpcServerConfig.class);

        List<RpcProcessor> processors = List.of(
            context.getBean(KVProcessor.class),
            context.getBean(PropertyProcessor.class),
            context.getBean(ClusterProcessor.class),
            context.getBean(RegistryProcessor.class),
            context.getBean(RouteProcessor.class),
            context.getBean(TopicProcessor.class)
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
