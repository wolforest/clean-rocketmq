package cn.coderule.minimq.registry.domain.property;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.RegistryConfig;
import cn.coderule.minimq.registry.processor.PropertyProcessor;
import cn.coderule.minimq.registry.server.bootstrap.ExecutorFactory;
import cn.coderule.minimq.registry.server.bootstrap.RegistryContext;
import cn.coderule.minimq.domain.config.network.RpcServerConfig;

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
