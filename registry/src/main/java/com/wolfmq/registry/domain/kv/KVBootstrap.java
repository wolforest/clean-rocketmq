package com.wolfmq.registry.domain.kv;


import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.processor.KVProcessor;
import com.wolfmq.registry.server.bootstrap.ExecutorFactory;

public class KVBootstrap implements Lifecycle {
    private final ApplicationContext context;

    public KVBootstrap(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        KVService kvService = new KVService(
            context.getBean(RegistryConfig.class)
        );

        ExecutorFactory factory = context.getBean(ExecutorFactory.class);
        KVProcessor processor = new KVProcessor(kvService, factory.getDefaultExecutor());

        context.register(kvService);
        context.register(processor);
    }

    @Override
    public void start() throws Exception { }

    @Override
    public void shutdown() throws Exception { }
}
