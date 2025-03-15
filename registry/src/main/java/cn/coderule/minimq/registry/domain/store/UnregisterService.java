package cn.coderule.minimq.registry.domain.store;

import cn.coderule.common.lang.concurrent.ServiceThread;
import cn.coderule.minimq.domain.config.RegistryConfig;

public class UnregisterService extends ServiceThread {
    private final RegistryConfig config;
    private final StoreManager manager;

    public UnregisterService(RegistryConfig config, StoreManager manager) {
        this.config = config;
        this.manager = manager;
    }

    @Override
    public String getServiceName() {
        return UnregisterService.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
