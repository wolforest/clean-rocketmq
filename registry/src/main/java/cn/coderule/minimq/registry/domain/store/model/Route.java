package cn.coderule.minimq.registry.domain.store.model;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.QueueMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;

@Data
public class Route implements Serializable {
    // topicMap: topicName -> groupName -> Topic
    private final ConcurrentMap<String, Map<String, Topic>> topicMap;
    // groupMap: groupName -> GroupInfo
    private final ConcurrentMap<String, GroupInfo> groupMap;
    // clusterMap: clusterName -> groupName
    private final ConcurrentMap<String, Set<String>> clusterMap;
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

    public void addGroupToCluster(String clusterName, String groupName) {
        Set<String> groupNames =this.clusterMap.computeIfAbsent(clusterName, k -> new HashSet<>());
        groupNames.add(groupName);
    }

    public boolean existGroup(String groupName) {
        return this.groupMap.containsKey(groupName);
    }

    public GroupInfo getGroup(String groupName) {
        return this.groupMap.get(groupName);
    }

    /**
     * get or create group
     * - get GroupInfo if exists
     * - create GroupInfo if not exists
     *    - add groupName to clusterMap
     *    - create GroupInfo
     *    - add GroupInfo to groupMap
     *
     * @param clusterName clusterName
     * @param groupName groupName
     * @return groupInfo
     */
    public GroupInfo getOrCreateGroup(String clusterName, String groupName) {
        GroupInfo groupInfo = this.groupMap.get(groupName);
        if (groupInfo != null) {
            return groupInfo;
        }

        addGroupToCluster(clusterName, groupName);

        groupInfo = new GroupInfo(clusterName, groupName, new HashMap<>());
        GroupInfo oldInfo = this.groupMap.putIfAbsent(groupName, groupInfo);

        return oldInfo == null ? groupInfo : oldInfo;
    }

}
