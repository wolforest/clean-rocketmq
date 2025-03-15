package cn.coderule.minimq.registry.domain.store;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.protocol.body.StoreRegisterResult;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreRegistry {
    private final RegistryConfig config;
    private final Route route;

    private final RpcClient rpcClient;
    private final UnregisterService unregisterService;

    public StoreRegistry(RegistryConfig config, RpcClient rpcClient, Route route) {
        this.route = route;
        this.config = config;

        this.rpcClient = rpcClient;
        this.unregisterService = new UnregisterService(config, this);
    }

    public void start() {
        unregisterService.start();
    }

    public void shutdown() {
        unregisterService.shutdown();
    }

    public StoreRegisterResult register(StoreInfo storeInfo, RouteInfo routeInfo, List<String> filterList) {
        return null;
    }

    public boolean unregisterAsync(UnRegisterBrokerRequestHeader request) {
        unregisterService.submit(request);
    }
}
