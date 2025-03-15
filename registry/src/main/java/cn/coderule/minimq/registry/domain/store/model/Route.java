package cn.coderule.minimq.registry.domain.store.model;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.rpc.registry.protocol.cluster.ClusterInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.QueueMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;

@Data
public class Route implements Serializable {
    // topicMap: topicName -> groupName -> Topic
    private final ConcurrentMap<String, Map<String, Topic>> topicMap;
    // groupMap: groupName -> GroupInfo
    private final ConcurrentMap<String, GroupInfo> groupMap;
    // clusterMap: clusterName -> ClusterInfo
    private final ConcurrentMap<String, ClusterInfo> clusterMap;
    // healthMap: storeInfo -> StoreHealthInfo
    private final ConcurrentMap<StoreInfo, StoreHealthInfo> healthMap;
    // filterMap: storeInfo -> filterServerList
    private final ConcurrentMap<StoreInfo, List<String>> filterMap;
    // topicQueue: topicName -> groupName -> QueueMap
    private final ConcurrentMap<String, Map<String, QueueMap>> topicQueueMap;

    public Route() {
        this.topicMap = new ConcurrentHashMap<>(1024);
        this.groupMap = new ConcurrentHashMap<>(128);
        this.clusterMap = new ConcurrentHashMap<>(32);
        this.healthMap = new ConcurrentHashMap<>(256);
        this.filterMap = new ConcurrentHashMap<>(256);
        this.topicQueueMap = new ConcurrentHashMap<>(1024);
    }
}
