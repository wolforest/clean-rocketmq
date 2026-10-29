package com.wolfmq.registry.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.config.network.RpcClientConfig;
import com.wolfmq.domain.config.network.RpcServerConfig;
import com.wolfmq.domain.config.server.RegistryConfig;
import com.wolfmq.domain.core.constant.ContextNamespace;

public class ConfigLoader {
    public static void load(ApplicationContext context) {
        RegistryConfig registryConfig = new RegistryConfig();
        context.register(registryConfig, ContextNamespace.APP);

        RpcServerConfig rpcServerConfig = new RpcServerConfig();
        rpcServerConfig.setPort(registryConfig.getPort());
        rpcServerConfig.setBossThreadNum(registryConfig.getBossThreadNum());
        rpcServerConfig.setWorkerThreadNum(registryConfig.getWorkerThreadNum());
        rpcServerConfig.setBusinessThreadNum(registryConfig.getBusinessThreadNum());
        rpcServerConfig.setCallbackThreadNum(registryConfig.getCallbackThreadNum());
        context.register(rpcServerConfig, ContextNamespace.APP);

        RpcClientConfig rpcClientConfig = new RpcClientConfig();
        context.register(rpcClientConfig, ContextNamespace.APP);
    }
}
