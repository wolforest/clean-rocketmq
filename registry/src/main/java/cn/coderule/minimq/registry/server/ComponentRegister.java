package cn.coderule.minimq.registry.server;

import cn.coderule.common.convention.service.LifecycleManager;

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
