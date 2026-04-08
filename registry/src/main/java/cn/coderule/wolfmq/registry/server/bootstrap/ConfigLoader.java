package cn.coderule.wolfmq.registry.server.bootstrap;

import cn.coderule.wolfmq.domain.config.server.RegistryConfig;
import cn.coderule.wolfmq.domain.config.network.RpcClientConfig;
import cn.coderule.wolfmq.domain.config.network.RpcServerConfig;

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
