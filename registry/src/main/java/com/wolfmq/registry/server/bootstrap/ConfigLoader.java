package com.wolfmq.registry.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.common.config.network.RpcClientConfig;
import com.wolfmq.common.config.network.RpcServerConfig;
import com.wolfmq.common.config.server.RegistryConfig;

public class ConfigLoader {
    public static void load(ApplicationContext context) {
        RegistryConfig registryConfig = new RegistryConfig();
        context.register(registryConfig);

        RpcServerConfig rpcServerConfig = new RpcServerConfig();
        rpcServerConfig.setPort(registryConfig.getPort());
        rpcServerConfig.setBossThreadNum(registryConfig.getBossThreadNum());
        rpcServerConfig.setWorkerThreadNum(registryConfig.getWorkerThreadNum());
        rpcServerConfig.setBusinessThreadNum(registryConfig.getBusinessThreadNum());
        rpcServerConfig.setCallbackThreadNum(registryConfig.getCallbackThreadNum());
        context.register(rpcServerConfig);

        RpcClientConfig rpcClientConfig = new RpcClientConfig();
        context.register(rpcClientConfig);
    }
}
