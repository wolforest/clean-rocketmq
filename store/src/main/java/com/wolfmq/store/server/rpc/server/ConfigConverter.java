package com.wolfmq.store.server.rpc.server;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.network.RpcServerConfig;

public class ConfigConverter {
    public static RpcServerConfig toServerConfig(StoreConfig storeConfig) {
        RpcServerConfig serverConfig = new RpcServerConfig();
        serverConfig.setAddress(storeConfig.getHost());
        serverConfig.setPort(storeConfig.getPort());

        serverConfig.setBossThreadNum(storeConfig.getBossThreadNum());
        serverConfig.setWorkerThreadNum(storeConfig.getWorkerThreadNum());
        serverConfig.setBusinessThreadNum(storeConfig.getBusinessThreadNum());
        serverConfig.setCallbackThreadNum(storeConfig.getCallbackThreadNum());

        return serverConfig;
    }
}
