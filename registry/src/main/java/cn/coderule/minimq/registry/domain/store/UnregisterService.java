package cn.coderule.minimq.registry.domain.store;

import cn.coderule.common.lang.concurrent.ServiceThread;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;

public class UnregisterService extends ServiceThread {
    private final RegistryConfig config;
    private final StoreRegistry manager;

    public UnregisterService(RegistryConfig config, StoreRegistry manager) {
        this.config = config;
        this.manager = manager;
    }

    @Override
    public String getServiceName() {
        return UnregisterService.class.getSimpleName();
    }

    public boolean submit(UnRegisterBrokerRequestHeader request) {
        return true;
    }

    @Override
    public void run() {

    }
}
