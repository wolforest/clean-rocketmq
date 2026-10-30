package com.wolfmq.registry.domain.property;


import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.processor.PropertyProcessor;
import com.wolfmq.registry.server.bootstrap.ExecutorFactory;
import com.wolfmq.domain.config.network.RpcServerConfig;

public class PropertyBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public PropertyBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        PropertyService propertyService = new PropertyService(
            context.getBean(RegistryConfig.class),
            context.getBean(RpcServerConfig.class)
        );

        ExecutorFactory factory = context.getBean(ExecutorFactory.class);
        PropertyProcessor processor = new PropertyProcessor(propertyService, factory.getDefaultExecutor());

        context.register(propertyService);
        context.register(processor);
    }

    @Override
    public void start() throws Exception { }

    @Override
    public void shutdown() throws Exception { }
}
