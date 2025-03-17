package cn.coderule.minimq.registry.domain.store.model;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.rpc.common.protocol.DataVersion;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.statictopic.TopicQueueMappingInfo;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Route implements Serializable {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

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
    private final ConcurrentMap<String, Map<String, TopicQueueMappingInfo>> topicQueueMap;

    public Route() {
        this.topicMap = new ConcurrentHashMap<>(1024);
        this.groupMap = new ConcurrentHashMap<>(128);
        this.clusterMap = new ConcurrentHashMap<>(32);
        this.healthMap = new ConcurrentHashMap<>(256);
        this.filterMap = new ConcurrentHashMap<>(256);
        this.topicQueueMap = new ConcurrentHashMap<>(1024);
    }

    public void lockWrite() throws InterruptedException {
        this.lock.writeLock().lockInterruptibly();
    }

    public void unlockWrite() {
        this.lock.writeLock().unlock();
    }

    public void lockRead() throws InterruptedException {
        this.lock.readLock().lockInterruptibly();
    }

    public void unlockRead() {
        this.lock.readLock().unlock();
    }

    public void addGroupToCluster(String clusterName, String groupName) {
        Set<String> groupNames =this.clusterMap.computeIfAbsent(clusterName, k -> new HashSet<>());
        groupNames.add(groupName);
    }

    public boolean containsGroup(String groupName) {
        return this.groupMap.containsKey(groupName);
    }

    public GroupInfo getGroup(String groupName) {
        return this.groupMap.get(groupName);
    }

    public GroupInfo removeGroup(String groupName) {
        return this.groupMap.remove(groupName);
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
    public GroupInfo getOrCreateGroup(String zoneName, String clusterName, String groupName, boolean enableActingMaster) {
        GroupInfo groupInfo = this.groupMap.get(groupName);
        if (groupInfo != null) {
            return groupInfo;
        }

        addGroupToCluster(clusterName, groupName);

        groupInfo = new GroupInfo(clusterName, groupName, new HashMap<>(), enableActingMaster, zoneName);
        GroupInfo oldInfo = this.groupMap.putIfAbsent(groupName, groupInfo);

        return oldInfo == null ? groupInfo : oldInfo;
    }

    public StoreHealthInfo getHealthInfo(StoreInfo storeInfo) {
        return this.healthMap.get(storeInfo);
    }

    public StoreHealthInfo saveHealthInfo(StoreInfo storeInfo, StoreHealthInfo healthInfo) {
        StoreHealthInfo prev = this.healthMap.put(storeInfo, healthInfo);
        if (prev != null) {
            log.info("StoreHealthInfo changed, Old: {}; NEW: {}", prev, healthInfo);
        }

        return prev;
    }

    public StoreHealthInfo removeHealthInfo(StoreInfo storeInfo) {
        return this.healthMap.remove(storeInfo);
    }

    public String getHealthHaAddress(StoreInfo storeInfo) {
        StoreHealthInfo healthInfo = this.healthMap.get(storeInfo);
        if (healthInfo == null) {
            return null;
        }

        return healthInfo.getHaServerAddr();
    }

    public DataVersion getHealthVersion(StoreInfo storeInfo) {
        StoreHealthInfo healthInfo = this.healthMap.get(storeInfo);
        if (healthInfo == null) {
            return null;
        }

        return healthInfo.getDataVersion();
    }

    public void removeFilter(StoreInfo storeInfo) {
        this.filterMap.remove(storeInfo);
    }

    public void saveFilter(StoreInfo storeInfo, List<String> filterList) {
        this.filterMap.put(storeInfo, filterList);
    }

    public Topic getTopic(String groupName,  String topicName) {
        if (!this.topicMap.containsKey(topicName)) {
            return null;
        }

        return this.topicMap.get(topicName).get(groupName);
    }

    public void saveTopic(String groupName, Topic topic) {
        Map<String, Topic> map = this.topicMap.computeIfAbsent(topic.getTopicName(), k -> new HashMap<>());
        if (map.containsKey(groupName)) {
            Topic oldTopic = map.get(groupName);
            log.info("Topic changed, Old: {}; NEW: {}", oldTopic, topic);
        }

        map.put(groupName, topic);
    }

    public Set<String> getTopicByGroup(String groupName) {
        Set<String> topicSet = new HashSet<>();
        for (Map.Entry<String, Map<String, Topic>> entry : this.topicMap.entrySet()) {
            if (entry.getValue().containsKey(groupName)) {
                topicSet.add(entry.getKey());
            }
        }
        return topicSet;
    }

    public void removeTopic(String groupName, String topicName) {
        Map<String, Topic> map = this.topicMap.get(topicName);
        if (map == null) {
            return;
        }
        Topic tmpTopic = map.remove(groupName);
        if (tmpTopic != null) {
            log.info("Topic removed, topic: {}, group: {}", topicName, groupName);
        }

        if (map.isEmpty()) {
            log.info("Topic removed, topic: {}", topicName);
            this.topicMap.remove(topicName);
        }
    }

    public boolean containsTopic(String groupName, String topicName) {
        Map<String, Topic> map = this.topicMap.get(topicName);
        if (map == null) {
            return false;
        }

        return map.containsKey(groupName);
    }

    public void saveQueueMap(String topicName, TopicQueueMappingInfo queueMap) {
        Map<String, TopicQueueMappingInfo> map = this.topicQueueMap.computeIfAbsent(topicName, k -> new HashMap<>());

        String groupName = queueMap.getBname();
        map.put(groupName, queueMap);
    }

    public Set<String> getGroupInCluster(String clusterName) {
        return this.clusterMap.get(clusterName);
    }

    public boolean removeGroupInCluster(String clusterName, String groupName) {
        Set<String> groupNames = this.clusterMap.get(clusterName);
        if (groupNames == null) {
            return false;
        }

        boolean status = groupNames.remove(groupName);
        log.info("unregisterBroker, remove name from clusterAddrTable {}, {}",
            status ? "OK" : "Failed",
            groupName
        );

        if (groupNames.isEmpty()) {
            this.clusterMap.remove(clusterName);
            log.info("unregisterBroker, remove cluster from clusterAddrTable {}",
                clusterName
            );
        }

        return status;
    }
}
