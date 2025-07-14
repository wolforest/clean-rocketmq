package cn.coderule.minimq.registry.domain.kv;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.RegistryConfig;
import cn.coderule.minimq.registry.processor.KVProcessor;
import cn.coderule.minimq.registry.server.bootstrap.ExecutorFactory;
import cn.coderule.minimq.registry.server.bootstrap.RegistryContext;

public class KVManager implements Lifecycle {
    @Override
    public void initialize() throws Exception {
        KVService kvService = new KVService(
            RegistryContext.getBean(RegistryConfig.class)
        );

        ExecutorFactory factory = RegistryContext.getBean(ExecutorFactory.class);
        KVProcessor processor = new KVProcessor(kvService, factory.getDefaultExecutor());

        RegistryContext.register(kvService);
        RegistryContext.register(processor);
    }

    @Override
    public void start() throws Exception { }

    @Override
    public void shutdown() throws Exception { }
}
