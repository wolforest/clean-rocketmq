package cn.coderule.minimq.store.server.rpc.server;

import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.rpc.common.config.RpcServerConfig;

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
