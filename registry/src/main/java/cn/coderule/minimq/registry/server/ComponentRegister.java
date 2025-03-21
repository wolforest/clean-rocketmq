package cn.coderule.minimq.registry.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.registry.server.context.RegistryContext;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();
        RegistryContext.register(register);

        return register.execute();
    }

    public LifecycleManager execute() {


        return this.manager;
    }



}
