package cn.coderule.minimq.registry.domain.property;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.rpc.common.config.Configuration;
import cn.coderule.minimq.rpc.common.config.RpcServerConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertyService {
    private final Configuration configuration;
    private final RegistryConfig registryConfig;
    private final RpcServerConfig rpcServerConfig;

    public PropertyService(RegistryConfig registryConfig, RpcServerConfig rpcServerConfig) {
        this.registryConfig = registryConfig;
        this.rpcServerConfig = rpcServerConfig;
        this.configuration = new Configuration(registryConfig, rpcServerConfig);
    }
}
