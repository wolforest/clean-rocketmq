package cn.coderule.minimq.registry.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.broker.BrokerManager;
import cn.coderule.minimq.registry.domain.kv.KVService;
import cn.coderule.minimq.registry.domain.property.PropertyService;
import cn.coderule.minimq.registry.domain.store.StoreManager;
import cn.coderule.minimq.registry.server.context.RegistryContext;
import cn.coderule.minimq.registry.server.rpc.RegistryServer;
import cn.coderule.minimq.rpc.common.config.RpcServerConfig;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();
        RegistryContext.register(register);

        return register.execute();
    }

    public LifecycleManager execute() {
        registerServer();

        registerProperty();
        registerKV();
        registerStore();
        registerBroker();

        return this.manager;
    }

    private void registerServer() {
        RegistryServer server = new RegistryServer();
        manager.register(server);
    }

    private void registerProperty() {
        PropertyService propertyService = new PropertyService(
            RegistryContext.getBean(RegistryConfig.class),
            RegistryContext.getBean(RpcServerConfig.class)
        );

        RegistryContext.register(propertyService);
    }

    private void registerKV() {
        KVService kvService = new KVService(
            RegistryContext.getBean(RegistryConfig.class)
        );

        RegistryContext.register(kvService);
    }

    private void registerBroker() {
        manager.register(new BrokerManager());
    }

    private void registerStore() {
        manager.register(new StoreManager());
    }

}
