package cn.coderule.minimq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.server.ExecutorFactory;
import cn.coderule.minimq.rpc.common.config.RpcServerConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegistryServer implements Lifecycle {
    private final RegistryConfig registryConfig;
    private final RpcServerConfig serverConfig;

    public RegistryServer(RegistryConfig registryConfig, RpcServerConfig serverConfig) {
        this.registryConfig = registryConfig;
        this.serverConfig = serverConfig;

    }

    @Override
    public void initialize() {
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void cleanup() {
    }


}
