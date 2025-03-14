package cn.coderule.minimq.registry.domain.store;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.protocol.body.StoreRegisterResult;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteManager {
    private final RegistryConfig config;
    private final RpcClient rpcClient;

    private ConcurrentMap<String, Map<String, Topic>> topicMap;
    private ConcurrentMap<String, GroupInfo> groupMap;
    private ConcurrentMap<String, ClusterInfo> clusterMap;

    public RouteManager(RegistryConfig config, RpcClient rpcClient) {
        this.config = config;
        this.rpcClient = rpcClient;
    }

    public StoreRegisterResult registerStore(StoreInfo storeInfo, RouteInfo routeInfo) {
        return null;
    }
}
