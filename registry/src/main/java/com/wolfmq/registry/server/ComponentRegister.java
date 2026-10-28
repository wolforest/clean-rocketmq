package com.wolfmq.registry.server;

import cn.coderule.common.convention.service.LifecycleManager;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.registry.domain.broker.BrokerManager;
import com.wolfmq.registry.domain.kv.KVManager;
import com.wolfmq.registry.domain.property.PropertyManager;
import com.wolfmq.registry.domain.store.StoreManager;
import com.wolfmq.registry.server.bootstrap.ExecutorFactory;
import com.wolfmq.registry.server.bootstrap.RegistryContext;
import com.wolfmq.registry.server.rpc.HaClient;
import com.wolfmq.registry.server.rpc.RpcManager;
import com.wolfmq.domain.config.network.RpcClientConfig;

/**
 * Component register, all components registered
 * will be initialized, start, shutdown, ... together.
 * - The order of registration is important.
 * - call RegistryContext.getBean() in xxxManager.initialize()
 * - only config related bean can be got by RegistryContext.getBean()
 */
public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        return new ComponentRegister().execute();
    }

    public LifecycleManager execute() {
        // global dependency
        registerExecutor();
        registerHaClient();

        // domain
        manager.register(new PropertyManager());
        manager.register(new KVManager());
        manager.register(new StoreManager());
        manager.register(new BrokerManager());

        // server
        manager.register(new RpcManager());
        return this.manager;
    }

    /**
     * register ExecutorService and ScheduledExecutorService, dependent by:
     * - RouteProcessor: routeExecutor
     * - other processors: defaultExecutor
     * - monitor: scheduler
     */
    private void registerExecutor() {
        RegistryConfig config = RegistryContext.getBean(RegistryConfig.class);
        ExecutorFactory factory = new ExecutorFactory(config);

        manager.register(factory);
        RegistryContext.register(factory);
    }

    /**
     * register HA Client, dependent by:
     * - StoreRegistry: notify M/S role change event
     */
    private void registerHaClient() {
        HaClient haClient = new HaClient(
            RegistryContext.getBean(RegistryConfig.class),
            RegistryContext.getBean(RpcClientConfig.class)
        );

        manager.register(haClient);
        RegistryContext.register(haClient);
    }

}
