package com.wolfmq.registry.server;

import cn.coderule.common.convention.container.ApplicationContext;
import cn.coderule.common.convention.service.LifecycleManager;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.registry.domain.broker.BrokerBootstrap;
import com.wolfmq.registry.domain.kv.KVBootstrap;
import com.wolfmq.registry.domain.property.PropertyBootstrap;
import com.wolfmq.registry.domain.store.StoreBootstrap;
import com.wolfmq.registry.server.bootstrap.ExecutorFactory;
import com.wolfmq.registry.server.rpc.HaClient;
import com.wolfmq.registry.server.rpc.RpcBootstrap;
import com.wolfmq.domain.config.network.RpcClientConfig;

/**
 * Component register, all components registered
 * will be initialized, start, shutdown, ... together.
 * - The order of registration is important.
 * - xxxManager.initialize() resolves dependencies from the ApplicationContext
 * - only config related beans are registered before managers initialize
 */
public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();
    private final ApplicationContext context;

    public ComponentRegister(ApplicationContext context) {
        this.context = context;
    }

    public LifecycleManager register() {
        return this.execute();
    }

    public LifecycleManager execute() {
        registerExecutor();
        registerHaClient();

        manager.register(new PropertyBootstrap(context));
        manager.register(new KVBootstrap(context));
        manager.register(new StoreBootstrap(context));
        manager.register(new BrokerBootstrap());

        manager.register(new RpcBootstrap(context));
        return this.manager;
    }

    private void registerExecutor() {
        RegistryConfig config = context.getBean(RegistryConfig.class, ContextNamespace.APP);
        ExecutorFactory factory = new ExecutorFactory(config);

        manager.register(factory);
        context.register(factory, ContextNamespace.APP);
    }

    private void registerHaClient() {
        HaClient haClient = new HaClient(
            context.getBean(RegistryConfig.class, ContextNamespace.APP),
            context.getBean(RpcClientConfig.class, ContextNamespace.APP)
        );

        manager.register(haClient);
        context.register(haClient, ContextNamespace.APP);
    }

}
