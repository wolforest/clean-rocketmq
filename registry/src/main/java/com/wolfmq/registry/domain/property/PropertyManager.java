package com.wolfmq.registry.domain.property;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.processor.PropertyProcessor;
import com.wolfmq.registry.server.bootstrap.ExecutorFactory;
import com.wolfmq.domain.config.network.RpcServerConfig;

public class PropertyManager implements Lifecycle {
    private final ApplicationContext context;

    public PropertyManager(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        PropertyService propertyService = new PropertyService(
            context.getBean(RegistryConfig.class, ContextNamespace.APP),
            context.getBean(RpcServerConfig.class, ContextNamespace.APP)
        );

        ExecutorFactory factory = context.getBean(ExecutorFactory.class, ContextNamespace.APP);
        PropertyProcessor processor = new PropertyProcessor(propertyService, factory.getDefaultExecutor());

        context.register(propertyService, ContextNamespace.APP);
        context.register(processor, ContextNamespace.APP);
    }

    @Override
    public void start() throws Exception { }

    @Override
    public void shutdown() throws Exception { }
}
