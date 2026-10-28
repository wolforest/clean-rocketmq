package com.wolfmq.registry.domain.property;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.processor.PropertyProcessor;
import com.wolfmq.registry.server.bootstrap.ExecutorFactory;
import com.wolfmq.registry.server.bootstrap.RegistryContext;
import com.wolfmq.domain.config.network.RpcServerConfig;

public class PropertyManager implements Lifecycle {
    @Override
    public void initialize() throws Exception {
        PropertyService propertyService = new PropertyService(
            RegistryContext.getBean(RegistryConfig.class),
            RegistryContext.getBean(RpcServerConfig.class)
        );

        ExecutorFactory factory = RegistryContext.getBean(ExecutorFactory.class);
        PropertyProcessor processor = new PropertyProcessor(propertyService, factory.getDefaultExecutor());

        RegistryContext.register(propertyService);
        RegistryContext.register(processor);
    }

    @Override
    public void start() throws Exception { }

    @Override
    public void shutdown() throws Exception { }
}
