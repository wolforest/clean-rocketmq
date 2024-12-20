package com.wolf.minimq.store.server;

import com.wolf.common.convention.service.LifecycleManager;

public class ComponentRegister {
    public static LifecycleManager register() {
        return new LifecycleManager();
    }
}
