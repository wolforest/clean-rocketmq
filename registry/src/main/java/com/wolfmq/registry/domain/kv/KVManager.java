package com.wolfmq.registry.domain.kv;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.processor.KVProcessor;
import com.wolfmq.registry.server.bootstrap.ExecutorFactory;

public class KVManager implements Lifecycle {
    private final ApplicationContext context;

    public KVManager(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize() throws Exception {
        KVService kvService = new KVService(
            context.getBean(RegistryConfig.class, ContextNamespace.APP)
        );

        ExecutorFactory factory = context.getBean(ExecutorFactory.class, ContextNamespace.APP);
        KVProcessor processor = new KVProcessor(kvService, factory.getDefaultExecutor());

        context.register(kvService, ContextNamespace.APP);
        context.register(processor, ContextNamespace.APP);
    }

    @Override
    public void start() throws Exception { }

    @Override
    public void shutdown() throws Exception { }
}
