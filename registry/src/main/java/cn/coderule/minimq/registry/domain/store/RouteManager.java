package cn.coderule.minimq.registry.domain.store;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.rpc.common.RpcClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteManager {
    private final RegistryConfig config;
    private final RpcClient rpcClient;



    public RouteManager(RegistryConfig config, RpcClient rpcClient) {
        this.config = config;
        this.rpcClient = rpcClient;
    }
}
