package com.wolfmq.registry.domain.kv;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.processor.KVProcessor;
import com.wolfmq.registry.server.bootstrap.ExecutorFactory;
import com.wolfmq.registry.server.bootstrap.RegistryContext;

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
