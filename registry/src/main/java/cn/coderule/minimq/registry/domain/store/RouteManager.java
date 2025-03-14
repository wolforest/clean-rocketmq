package cn.coderule.minimq.registry.domain.store;

import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.protocol.body.StoreRegisterResult;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.QueueMap;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteManager {
    private final RegistryConfig config;
    private final RpcClient rpcClient;

    // topicMap: topicName -> groupName -> Topic
    private ConcurrentMap<String, Map<String, Topic>> topicMap;
    // groupMap: groupName -> GroupInfo
    private ConcurrentMap<String, GroupInfo> groupMap;
    // clusterMap: clusterName -> ClusterInfo
    private ConcurrentMap<String, ClusterInfo> clusterMap;
    // healthMap: storeInfo -> StoreHealthInfo
    private ConcurrentMap<StoreInfo, StoreHealthInfo> healthMap;
    // filterMap: storeInfo -> filterServerList
    private ConcurrentMap<StoreInfo, List<String>> filterMap;
    // topicQueue: topicName -> groupName -> QueueMap
    private ConcurrentMap<String, Map<String, QueueMap>> topicQueueMap;

    public RouteManager(RegistryConfig config, RpcClient rpcClient) {
        this.config = config;
        this.rpcClient = rpcClient;
    }

    public StoreRegisterResult registerStore(StoreInfo storeInfo, RouteInfo routeInfo, List<String> filterList) {
        return null;
    }
}
