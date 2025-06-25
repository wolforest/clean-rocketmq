package cn.coderule.minimq.registry.domain.store;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.server.RegistryConfig;
import cn.coderule.minimq.domain.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.domain.core.constant.PermName;
import cn.coderule.minimq.domain.domain.model.meta.topic.Topic;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.registry.domain.store.model.StoreHealthInfo;
import cn.coderule.minimq.registry.domain.store.model.StoreStatusInfo;
import cn.coderule.minimq.registry.domain.store.service.UnregisterService;
import cn.coderule.minimq.registry.server.rpc.HaClient;
import cn.coderule.minimq.domain.domain.model.meta.DataVersion;
import cn.coderule.minimq.rpc.registry.protocol.body.RegisterStoreResult;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigAndMappingSerializeWrapper;
import cn.coderule.minimq.domain.domain.model.meta.topic.TopicConfigSerializeWrapper;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.GroupInfo;
import cn.coderule.minimq.domain.domain.model.cluster.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import cn.coderule.minimq.domain.domain.model.meta.statictopic.TopicQueueMappingInfo;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoreRegistry {
    private static final long DEFAULT_BROKER_CHANNEL_EXPIRED_TIME = 1000 * 60 * 2;

    private final RegistryConfig config;
    private final Route route;

    private final HaClient haClient;
    private final UnregisterService unregisterService;

    public StoreRegistry(RegistryConfig config, Route route, HaClient haClient) {
        this.route = route;
        this.config = config;

        this.haClient = haClient;
        this.unregisterService = new UnregisterService(config, this);
    }

    public void start() {
        unregisterService.start();
    }

    public void shutdown() {
        unregisterService.shutdown();
    }

    public RegisterStoreResult register(StoreInfo store, Channel channel) {
        RegisterStoreResult result = new RegisterStoreResult();
        try {
            route.lockWrite();

            GroupInfo group = getOrCreateGroup(store);
            boolean isMinIdChanged = checkMinIdChanged(store, group);
            removeExistAddress(store, group);

            if (!checkHealthInfo(store, group, store.getTopicInfo())) {
                return result;
            }
            if (hasRegistered(store, group, store.getTopicInfo())) {
                return null;
            }

            boolean isFirst = saveAddress(store, group);
            registerTopicInfo(store, group, store.getTopicInfo(), isFirst);
            saveHealthInfo(store, store.getTopicInfo(), channel);
            saveFilterList(store, store.getFilterList());
            setHaAndMasterInfo(store, result);

            if (isMinIdChanged) {
                haClient.notifyMinIdChanged(group, route.getHealthHaAddress(store));
            }
        } catch (Exception e) {
            log.error("register store error", e);
        } finally {
            route.unlockWrite();
        }
        return result;
    }

    public void unregister(Set<UnRegisterBrokerRequestHeader> requests) {
        try {
            route.lockWrite();

            Set<String> removedSet = new HashSet<>();
            Set<String> reducedSet = new HashSet<>();
            Map<String, StoreStatusInfo> notifyMap = new HashMap<>();

            for (UnRegisterBrokerRequestHeader request : requests) {
                unregister(request, removedSet, reducedSet, notifyMap);
            }

            cleanTopicWhileUnRegister(removedSet, reducedSet);
            haClient.notifyMinIdChanged(notifyMap, route);
        } catch (Exception e) {
            log.error("register store error", e);
        } finally {
            route.unlockWrite();
        }
    }

    public boolean unregisterAsync(UnRegisterBrokerRequestHeader request) {
        return unregisterService.submit(request);
    }

    public void registerTopic(String groupName, List<Topic> topicList) {
        if (CollectionUtil.isEmpty(topicList)) {
            return;
        }

        try {
            route.lockWrite();
            for (Topic topic : topicList) {
                route.saveTopic(groupName, topic);
            }
        } catch (Exception e) {
            log.error("register topic error", e);
        } finally {
            route.unlockWrite();
        }
    }

    private boolean isStoreChanged(StoreInfo store, DataVersion version) {
        DataVersion prevVersion = route.getHealthVersion(store);
        return prevVersion == null || !prevVersion.equals(version);
    }

    private boolean isTopicChanged(StoreInfo store,  String topicName, DataVersion version) {
        if (isStoreChanged(store, version)) {
            return true;
        }

        return !route.containsTopic(store.getGroupName(), topicName);
    }

    private GroupInfo getOrCreateGroup(StoreInfo storeInfo) {
        return route.getOrCreateGroup(
            storeInfo.getZoneName(),
            storeInfo.getClusterName(),
            storeInfo.getGroupName(),
            storeInfo.isEnableMasterElection()
        );
    }

    private boolean saveAddress(StoreInfo store, GroupInfo group) {
        String preAddr = group.putAddress(store.getGroupNo(), store.getAddress());
        return StringUtil.isEmpty(preAddr);
    }

    private boolean checkMinIdChanged(StoreInfo store, GroupInfo group) {
        Map<Long, String> addrMap = group.getBrokerAddrs();

        boolean isMinIdChanged = false;
        long preMinId = 0;
        if (!addrMap.isEmpty()) {
            preMinId = Collections.min(addrMap.keySet());
        }

        if (store.getGroupNo() < preMinId) {
            isMinIdChanged = true;
        }

        return isMinIdChanged;
    }

    private void removeExistAddress(StoreInfo store, GroupInfo group) {
        Map<Long, String> addrMap = group.getBrokerAddrs();

        //Switch slave to master: first remove <1, IP:PORT> in namesrv, then add <0, IP:PORT>
        //The same IP:PORT must only have one record in brokerAddrTable
        addrMap.entrySet().removeIf(
            item -> null != store.getAddress()
                && store.getAddress().equals(item.getValue())
                && store.getGroupNo() != item.getKey()
        );
    }

    private boolean checkHealthInfo(StoreInfo store, GroupInfo group, TopicConfigSerializeWrapper topicInfo) {
        Map<Long, String> addrMap = group.getBrokerAddrs();
        String oldAddr = addrMap.get(store.getGroupNo());
        if (null == oldAddr || oldAddr.equals(store.getAddress())) {
            return true;
        }

        StoreInfo oldStore = new StoreInfo(store.getClusterName(), oldAddr);
        StoreHealthInfo oldHealthInfo = route.getHealthInfo(oldStore);
        if (oldHealthInfo == null) {
            return true;
        }

        long oldVersion = oldHealthInfo.getDataVersion().getStateVersion();
        long newVersion = topicInfo.getDataVersion().getStateVersion();
        if (oldVersion <= newVersion) {
            return true;
        }

        log.warn("Registering Broker conflicts with the existed one, just ignore.:"
                + " Cluster:{}, BrokerName:{}, BrokerId:{}, Old BrokerAddr:{}, "
                + "Old Version:{}, New BrokerAddr:{}, New Version:{}.",
                store.getClusterName(), store.getGroupName(), store.getGroupNo(),
                oldAddr, oldVersion, store.getAddress(), newVersion
        );

        //Remove the rejected brokerAddr from brokerLiveTable.
        route.removeHealthInfo(store);

        return false;
    }

    private boolean hasRegistered(StoreInfo store, GroupInfo group, TopicConfigSerializeWrapper topicInfo) {
        if (group.getBrokerAddrs().containsKey(store.getGroupNo())) {
            return false;
        }

        if (topicInfo.getTopicConfigTable().size() != 1) {
            return false;
        }

        log.warn("Can't register topicConfigWrapper={} because broker[{}]={} has not registered.",
            topicInfo.getTopicConfigTable(), store.getGroupNo(), store.getAddress());

        return true;
    }

    private boolean isPrimarySlave(StoreInfo store, GroupInfo group) {
        return  MQConstants.MASTER_ID != store.getGroupNo()
            && null == store.getEnableMasterElection()
            && store.getGroupNo() == group.getMinNo();
    }

    private Map<String, TopicQueueMappingInfo> getQueueMap(TopicConfigSerializeWrapper topicInfo) {
        TopicConfigAndMappingSerializeWrapper topicWrapper = TopicConfigAndMappingSerializeWrapper.from(topicInfo);
        return topicWrapper.getTopicQueueMappingInfoMap();
    }

    /**
     * Delete the topics that don't exist in tcTable from the current broker
     * Static topic is not supported currently
     *
     * @param store store
     * @param topicInfo topicInfo
     */
    private void deleteNotExistTopic(StoreInfo store, TopicConfigSerializeWrapper topicInfo) {
        // false in default setting
        if (!config.isDeleteTopicWhileRegistration()) {
            return;
        }

        Set<String> oldTopicSet = route.getTopicByGroup(store.getGroupName());
        Set<String> newTopicSet = topicInfo.getTopicConfigTable().keySet();
        Sets.SetView<String> diffSet = Sets.difference(oldTopicSet, newTopicSet);

        for (String topicName : diffSet) {
            route.removeTopic(store.getGroupName(), topicName);
        }
    }

    private void saveTopicInfo(StoreInfo store, GroupInfo group, TopicConfigSerializeWrapper topicInfo, boolean isFirst) {
        if (null == topicInfo || null == topicInfo.getTopicConfigTable()) {
            return;
        }

        boolean isMaster = MQConstants.MASTER_ID == store.getGroupNo();
        boolean isPrimarySlave = isPrimarySlave(store, group);
        if (!isMaster && !isPrimarySlave) {
            return;
        }

        Map<String, TopicQueueMappingInfo> queueMap = getQueueMap(topicInfo);
        if (queueMap.isEmpty()) {
            deleteNotExistTopic(store, topicInfo);
        }

        saveQueueInfo(store, topicInfo, isFirst, isPrimarySlave);
        saveQueueMap(store, topicInfo, isFirst, queueMap);
    }

    private void saveQueueInfo(StoreInfo store, TopicConfigSerializeWrapper topicInfo, boolean isFirst, boolean isPrimarySlave) {
        for (Map.Entry<String, Topic> entry : topicInfo.getTopicConfigTable().entrySet()) {
            if (!isFirst && !isTopicChanged(store,entry.getKey(), topicInfo.getDataVersion() )) {
                continue;
            }

            Topic topic = entry.getValue();
            if (isPrimarySlave && store.isEnableMasterElection()) {
                topic.setPerm(topic.getPerm() & (~PermName.PERM_WRITE));
            }

            route.saveTopic(store.getGroupName(), topic);
        }
    }

    private void saveQueueMap(StoreInfo store, TopicConfigSerializeWrapper topicInfo, boolean isFirst, Map<String, TopicQueueMappingInfo> queueMap) {
        if (!isStoreChanged(store, topicInfo.getDataVersion()) && !isFirst) {
            return;
        }
        for (Map.Entry<String, TopicQueueMappingInfo> entry : queueMap.entrySet()) {
            route.saveQueueMap(entry.getKey(), entry.getValue());
        }
    }

    private void registerTopicInfo(StoreInfo store, GroupInfo group, TopicConfigSerializeWrapper topicInfo, boolean isFirst) {
        if (null == topicInfo) {
            return;
        }

        boolean isMaster = MQConstants.MASTER_ID == store.getGroupNo();
        boolean isPrimarySlave = isPrimarySlave(store, group);
        if (!isMaster && !isPrimarySlave) {
            return;
        }

        ConcurrentMap<String, Topic> topicTable = topicInfo.getTopicConfigTable();
        if (topicTable == null) {
            return;
        }

        Map<String, TopicQueueMappingInfo> queueMap = getQueueMap(topicInfo);
        if (config.isDeleteTopicWhileRegistration() && queueMap.isEmpty()) {
            deleteNotExistTopic(store, topicInfo);
        }

        saveTopicInfo(store, group, topicInfo, isFirst);
        saveQueueMap(store, topicInfo, isFirst, queueMap);
    }

    private void saveHealthInfo(StoreInfo store, TopicConfigSerializeWrapper topicInfo, Channel channel) {
        long timeout = null != store.getHeartbeatTimeout()
            ? store.getHeartbeatTimeout()
            : DEFAULT_BROKER_CHANNEL_EXPIRED_TIME;
        DataVersion version = null != topicInfo
            ? topicInfo.getDataVersion()
            : new DataVersion();

        StoreHealthInfo healthInfo = StoreHealthInfo.builder()
            .lastUpdateTimestamp(System.currentTimeMillis())
            .heartbeatTimeoutMillis(timeout)
            .dataVersion(version)
            .channel(channel)
            .haServerAddr(store.getHaAddress())
            .build();

        route.saveHealthInfo(store, healthInfo);
    }

    private void saveFilterList(StoreInfo store, List<String> filterList) {
        if (null == filterList) {
            return;
        }

        if (filterList.isEmpty()) {
            route.removeFilter(store);
            return;
        }

        route.saveFilter(store, filterList);
    }

    private void setHaAndMasterInfo(StoreInfo store, RegisterStoreResult result) {
        if (MQConstants.MASTER_ID == store.getGroupNo()) {
            return;
        }

        String masterAddr = result.getMasterAddr();
        if (masterAddr == null) {
            return;
        }

        StoreInfo masterStore = new StoreInfo(store.getClusterName(), masterAddr);
        StoreHealthInfo masterHealthInfo = route.getHealthInfo(masterStore);
        if (masterHealthInfo == null) {
            return;
        }

        result.setMasterAddr(masterAddr);
        result.setHaServerAddr(masterHealthInfo.getHaServerAddr());
    }

    private void cleanRemovedStore(Set<String> removedBroker, Map<String, Topic> topicMap, String topic) {
        for (final String tmpGroup : removedBroker) {
            final Topic removedQD = topicMap.remove(tmpGroup);
            if (removedQD != null) {
                log.debug("removeTopicByBrokerName, remove one broker's topic {} {}", topic, removedQD);
            }
        }
    }

    private void cleanReducedStore(Set<String> reducedBroker, Map<String, Topic> topicMap) {
        for (final String groupName : reducedBroker) {
            final Topic tmpTopic = topicMap.get(groupName);
            if (tmpTopic == null) {
                continue;
            }

            GroupInfo groupInfo = route.getGroup(groupName);
            if (!groupInfo.isEnableActingMaster()) {
                continue;
            }

            // Master has been unregistered, wipe the write perm
            if (noMasterInGroup(groupInfo)) {
                tmpTopic.setPerm(tmpTopic.getPerm() & (~PermName.PERM_WRITE));
            }
        }
    }

    private void cleanTopicWhileUnRegister(Set<String> removedBroker, Set<String> reducedBroker) {
        Iterator<Map.Entry<String, Map<String, Topic>>> itMap = route.getTopicMap().entrySet().iterator();
        while (itMap.hasNext()) {
            Map.Entry<String, Map<String, Topic>> entry = itMap.next();
            Map<String, Topic> topicMap = entry.getValue();

            cleanRemovedStore(removedBroker, topicMap, entry.getKey());

            if (topicMap.isEmpty()) {
                log.debug("removeTopicByBrokerName, remove the topic all queue {}", entry.getKey());
                itMap.remove();
            }

            cleanReducedStore(reducedBroker, topicMap);
        }
    }

    private boolean noMasterInGroup(GroupInfo groupInfo) {
        if (groupInfo == null) {
            return true;
        }

        if (groupInfo.isAddressEmpty()) {
            return true;
        }

        return groupInfo.getMinNo() > 0;
    }

    private void removeGroupInfo(StoreInfo store, GroupInfo group, Map<String, StoreStatusInfo> notifyMap) {
        if (null == group) {
            return;
        }
        boolean isMinIdChanged = !group.getBrokerAddrs().isEmpty()
            && store.getGroupNo() == group.getMinNo();

        boolean removed = group.getBrokerAddrs().entrySet()
            .removeIf(item -> item.getValue().equals(store.getAddress()));
        log.info("unregisterBroker, remove addr from brokerAddrTable {}, {}",
            removed ? "OK" : "Failed",
            store
        );

        if (group.getBrokerAddrs().isEmpty()) {
            route.removeGroup(store.getGroupName());
            log.info("unregisterBroker, remove group from brokerGroupTable {}", store.getGroupName());
            return;
        }

        if (!isMinIdChanged) {
            return;
        }

        StoreStatusInfo statusInfo = StoreStatusInfo.builder()
            .brokerAddrs(group.getBrokerAddrs())
            .offlineBrokerAddr(store.getAddress())
            .build();
        notifyMap.put(store.getGroupName(), statusInfo);
    }

    private void removeClusterInfo(StoreInfo store, GroupInfo group, Set<String> removedSet, Set<String> reducedSet) {
        if (null == group || group.isAddressEmpty()) {
            reducedSet.add(store.getGroupName());
            return;
        }

        route.removeGroupInCluster(group.getCluster(), group.getBrokerName());
        removedSet.add(group.getBrokerName());
    }

    private void unregister(UnRegisterBrokerRequestHeader request, Set<String> removedSet, Set<String> reducedSet, Map<String, StoreStatusInfo> notifyMap) {
        StoreInfo store = new StoreInfo(request.getClusterName(), request.getBrokerAddr());
        store.setGroupName(request.getBrokerName());
        store.setGroupNo(request.getBrokerId());

        StoreHealthInfo healthInfo = route.removeHealthInfo(store);
        log.info("unregisterBroker, remove from brokerLiveTable {}, {}",
            healthInfo != null ? "OK" : "Failed",
            request.getBrokerAddr()
        );

        route.removeFilter(store);

        GroupInfo group = route.getGroup(store.getGroupName());
        removeGroupInfo(store, group, notifyMap);
        removeClusterInfo(store, group, removedSet, reducedSet);
    }
}
