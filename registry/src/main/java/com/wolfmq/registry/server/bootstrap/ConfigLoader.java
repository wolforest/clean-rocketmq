package com.wolfmq.registry.server.bootstrap;

import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.domain.config.network.RpcClientConfig;
import com.wolfmq.domain.config.network.RpcServerConfig;

public class ConfigLoader {
    public static void load() {
        RegistryConfig registryConfig = new RegistryConfig();
        RegistryContext.register(registryConfig);

        RpcServerConfig rpcServerConfig = new RpcServerConfig();
        rpcServerConfig.setPort(registryConfig.getPort());
        rpcServerConfig.setBossThreadNum(registryConfig.getBossThreadNum());
        rpcServerConfig.setWorkerThreadNum(registryConfig.getWorkerThreadNum());
        rpcServerConfig.setBusinessThreadNum(registryConfig.getBusinessThreadNum());
        rpcServerConfig.setCallbackThreadNum(registryConfig.getCallbackThreadNum());
        RegistryContext.register(rpcServerConfig);

        RpcClientConfig rpcClientConfig = new RpcClientConfig();
        RegistryContext.register(rpcClientConfig);
    }
}
