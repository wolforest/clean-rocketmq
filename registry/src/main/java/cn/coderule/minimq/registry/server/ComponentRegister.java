package cn.coderule.minimq.registry.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.broker.BrokerManager;
import cn.coderule.minimq.registry.domain.kv.KVManager;
import cn.coderule.minimq.registry.domain.property.PropertyManager;
import cn.coderule.minimq.registry.domain.store.StoreManager;
import cn.coderule.minimq.registry.server.context.RegistryContext;
import cn.coderule.minimq.registry.server.rpc.HaClient;
import cn.coderule.minimq.registry.server.rpc.ServerManager;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();
        RegistryContext.register(register);

        return register.execute();
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
        manager.register(new ServerManager());
        return this.manager;
    }

    private void registerExecutor() {
        RegistryConfig config = RegistryContext.getBean(RegistryConfig.class);
        ExecutorFactory factory = new ExecutorFactory(config);

        manager.register(factory);
        RegistryContext.register(factory);
    }

    private void registerHaClient() {
        HaClient haClient = new HaClient(
            RegistryContext.getBean(RegistryConfig.class),
            RegistryContext.getBean(RpcClientConfig.class)
        );

        manager.register(haClient);
        RegistryContext.register(haClient);
    }

}
