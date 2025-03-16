package cn.coderule.minimq.registry.domain.store;

import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.domain.constant.MQConstants;
import cn.coderule.minimq.domain.constant.PermName;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.registry.domain.store.model.StoreHealthInfo;
import cn.coderule.minimq.registry.domain.store.model.StoreStatusInfo;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.registry.protocol.body.StoreRegisterResult;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigAndMappingSerializeWrapper;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicConfigSerializeWrapper;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.cluster.StoreInfo;
import cn.coderule.minimq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.protocol.statictopic.TopicQueueMappingInfo;
import java.nio.channels.Channel;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
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

    private GroupInfo getOrCreateGroup(StoreInfo storeInfo) {
        return route.getOrCreateGroup(
            storeInfo.getZoneName(),
            storeInfo.getClusterName(),
            storeInfo.getGroupName(),
            storeInfo.isEnableActingMaster()
        );
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
            && null == store.getEnableActingMaster()
            && store.getGroupNo() == group.getMinNo();
    }

    private Map<String, TopicQueueMappingInfo> getQueueMap(TopicConfigSerializeWrapper topicInfo) {
        TopicConfigAndMappingSerializeWrapper topicWrapper = TopicConfigAndMappingSerializeWrapper.from(topicInfo);
        return topicWrapper.getTopicQueueMappingInfoMap();
    }

    private void deleteNotExistTopic(StoreInfo store, TopicConfigSerializeWrapper topicInfo) {
        if (!config.isDeleteTopicWhileRegistration()) {
            return;
        }

        // Delete the topics that don't exist in tcTable from the current broker
        // Static topic is not supported currently
        // false in default setting
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

    private boolean isTopicChanged(StoreInfo store, TopicConfigSerializeWrapper topicInfo, String topicName) {

        return false;
    }

    private void saveQueueInfo(StoreInfo store, TopicConfigSerializeWrapper topicInfo, boolean isFirst, boolean isPrimarySlave) {
        for (Map.Entry<String, Topic> entry : topicInfo.getTopicConfigTable().entrySet()) {
            if (!isFirst && !isTopicChanged(store, topicInfo, entry.getKey())) {
                continue;
            }

            Topic topic = entry.getValue();
            if (isPrimarySlave && store.isEnableActingMaster()) {
                topic.setPerm(topic.getPerm() & (~PermName.PERM_WRITE));
            }

            route.saveTopic(store.getGroupName(), topic);
        }

    }

    private void saveQueueMap(StoreInfo store, TopicConfigSerializeWrapper topicInfo, boolean isFirst, Map<String, TopicQueueMappingInfo> queueMap) {

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

    public void saveHealthInfo(StoreInfo store, TopicConfigSerializeWrapper topicInfo, Channel channel) {

    }

    private void saveFilterList(StoreInfo store, List<String> filterList) {

    }

    private void setHaAndMasterInfo(StoreInfo store, GroupInfo group, StoreRegisterResult result) {

    }

    private void notifyMinIdChanged(Map<String, StoreStatusInfo> notifyMap) {

    }

    private void notifyMinIdChanged() {
        if (!config.isNotifyMinIdChanged()) {
            return;
        }
    }

    public StoreRegisterResult register(StoreInfo store, TopicConfigSerializeWrapper topicInfo, List<String> filterList, Channel channel) {
        StoreRegisterResult result = new StoreRegisterResult();
        try {
            route.lockWrite();

            GroupInfo group = getOrCreateGroup(store);
            boolean isMinIdChanged = checkMinIdChanged(store, group);
            removeExistAddress(store, group);

            if (!checkHealthInfo(store, group, topicInfo)) {
                return result;
            }

            if (hasRegistered(store, group, topicInfo)) {
                return null;
            }

            String preAddr = group.putAddress(store.getGroupNo(), store.getAddress());
            boolean isFirst = StringUtil.isEmpty(preAddr);

            registerTopicInfo(store, group, topicInfo, isFirst);
            saveHealthInfo(store, topicInfo, channel);
            saveFilterList(store, filterList);
            setHaAndMasterInfo(store, group, result);

            if (isMinIdChanged) notifyMinIdChanged();
        } catch (Exception e) {
            log.error("register store error", e);
        } finally {
            route.unlockWrite();
        }

        return result;
    }

    private void cleanTopicWhileUnRegister(Set<String> removedBroker, Set<String> reducedBroker) {

    }

    public void unregister(UnRegisterBrokerRequestHeader request, Set<String> removedSet, Set<String> reducedSet, Map<String, StoreStatusInfo> notifyMap) {
        StoreInfo store = new StoreInfo(request.getClusterName(), request.getBrokerAddr());

        StoreHealthInfo healthInfo = route.removeHealthInfo(store);
        log.info("unregisterBroker, remove from brokerLiveTable {}, {}",
            healthInfo != null ? "OK" : "Failed",
            request.getBrokerAddr()
        );

        route.removeFilter(store);

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
            notifyMinIdChanged(notifyMap);
        } catch (Exception e) {
            log.error("register store error", e);
        } finally {
            route.unlockWrite();
        }
    }

    public boolean unregisterAsync(UnRegisterBrokerRequestHeader request) {
        return unregisterService.submit(request);
    }
}
