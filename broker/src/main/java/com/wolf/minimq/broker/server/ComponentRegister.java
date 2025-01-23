package com.wolf.minimq.broker.server;

import com.wolf.common.convention.service.LifecycleManager;
import com.wolf.minimq.broker.server.grpc.GrpcManager;
import com.wolf.minimq.domain.service.store.manager.MetaManager;
import com.wolf.minimq.store.domain.meta.DefaultMetaManager;
import com.wolf.minimq.store.server.StoreContext;

public class ComponentRegister {
    private final LifecycleManager manager = new LifecycleManager();

    public static LifecycleManager register() {
        ComponentRegister register = new ComponentRegister();
        StoreContext.register(register);

        return register.execute();
    }

    public LifecycleManager execute() {

        registerGrpc();


        return this.manager;
    }

    private void registerGrpc() {
        GrpcManager component = new GrpcManager();
        manager.register(component);
    }

}
