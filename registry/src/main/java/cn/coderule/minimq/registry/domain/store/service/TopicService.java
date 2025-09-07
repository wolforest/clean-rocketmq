package cn.coderule.minimq.registry.domain.store.service;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.minimq.domain.config.server.RegistryConfig;
import cn.coderule.minimq.domain.core.constant.flag.TopicSysFlag;
import cn.coderule.minimq.domain.core.enums.message.MessageType;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicList;
import cn.coderule.minimq.domain.domain.cluster.server.GroupInfo;
import cn.coderule.minimq.domain.domain.cluster.server.StoreInfo;
import cn.coderule.minimq.domain.domain.cluster.route.QueueInfo;
import cn.coderule.minimq.domain.domain.cluster.route.RouteInfo;
import cn.coderule.minimq.domain.domain.meta.statictopic.TopicQueueMappingInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicService {
    private final Route route;

    public TopicService(RegistryConfig config, Route route) {
        this.route = route;
    }

    public RouteInfo getRoute(String topicName) {
        RouteInfo routeInfo = new RouteInfo();

        try {
            route.lockRead();

            if (!getQueueList(routeInfo, topicName)) {
                return null;
            }

            getGroupInfo(routeInfo, topicName);
            getQueueMap(routeInfo, topicName);
        } catch (Exception e) {
            log.error("getRoute error", e);
        } finally {
            route.unlockRead();
        }

        handleActingMaster(routeInfo, topicName);

        log.debug("get route info, topic: {}, routeInfo: {}", topicName, routeInfo);
        return routeInfo;
    }

    public TopicList getTopicList() {
        TopicList topicList = new TopicList();

        try {
            route.lockRead();
            topicList.setTopicList(route.getTopicMap().keySet());
        } catch (Exception e) {
            log.error("getTopicList error", e);
        } finally {
            route.unlockRead();
        }

        return topicList;
    }

    public TopicList getUnitTopic() {
        TopicList topicList = new TopicList();

        try {
            route.lockRead();

            for (Map.Entry<String, Map<String, Topic>> topicEntry : route.getTopicMap().entrySet()) {
                getUnitTopic(topicList, topicEntry);
            }
        } catch (Exception e) {
            log.error("getUnitTopic error", e);
        } finally {
            route.unlockRead();
        }

        return topicList;
    }

    public TopicList getSubUnitTopic() {
        TopicList topicList = new TopicList();

        try {
            route.lockRead();

            for (Map.Entry<String, Map<String, Topic>> topicEntry : this.route.getTopicMap().entrySet()) {
                String topic = topicEntry.getKey();
                Map<String, Topic> topicMap = topicEntry.getValue();
                if (MapUtil.isEmpty(topicMap)) {
                    continue;
                }

                int sysFlag = topicMap.values().iterator().next().getTopicSysFlag();
                if (TopicSysFlag.hasUnitSubFlag(sysFlag)) {
                    topicList.getTopicList().add(topic);
                }
            }

        } catch (Exception e) {
            log.error("getSubUnitTopic error", e);
        } finally {
            route.unlockRead();
        }

        return topicList;
    }

    public TopicList getSubAndNoUnitTopic() {
        TopicList topicList = new TopicList();

        try {
            route.lockRead();

            for (Map.Entry<String, Map<String, Topic>> topicEntry : this.route.getTopicMap().entrySet()) {
                String topic = topicEntry.getKey();
                Map<String, Topic> topicMap = topicEntry.getValue();
                if (MapUtil.isEmpty(topicMap)) {
                    continue;
                }

                int sysFlag = topicMap.values().iterator().next().getTopicSysFlag();
                if (!TopicSysFlag.hasUnitFlag(sysFlag) && TopicSysFlag.hasUnitSubFlag(sysFlag)) {
                    topicList.getTopicList().add(topic);
                }
            }

        } catch (Exception e) {
            log.error("getSubAndNoUnitTopic error", e);
        } finally {
            route.unlockRead();
        }

        return topicList;
    }

    public TopicList getTopicByCluster(String clusterName) {
        TopicList topicList = new TopicList();

        try {
            route.lockRead();

            Set<String> groupSet = route.getGroupInCluster(clusterName);
            if (CollectionUtil.isEmpty(groupSet)) {
                return topicList;
            }

            for (String groupName : groupSet) {
                getTopicByGroup(topicList, groupName);
            }
        } catch (Exception e) {
            log.error("getTopicList error", e);
        } finally {
            route.unlockRead();
        }

        return topicList;
    }

    public TopicList getSystemTopicList() {
        TopicList topicList = new TopicList();

        try {
            route.lockRead();

            for (Map.Entry<String, Set<String>> entry: route.getClusterMap().entrySet()) {
                topicList.getTopicList().add(entry.getKey());
                topicList.getTopicList().addAll(entry.getValue());
            }

            getFirstAddressFromGroupMap(topicList);
        } catch (Exception e) {
            log.error("getSystemTopicList error", e);
        } finally {
            route.unlockRead();
        }

        return topicList;
    }

    public void deleteTopic(String topicName) {
        try {
            route.lockWrite();
            route.removeTopic(topicName);
        } catch (Exception e) {
            log.error("delete topic error", e);
        } finally {
            route.unlockWrite();
        }
    }

    public void deleteTopic(String topicName, String clusterName) {
        try {
            route.lockWrite();

            Set<String> groupSet = route.getGroupInCluster(clusterName);
            if (CollectionUtil.isEmpty(groupSet)) {
                return;
            }

            for (String groupName : groupSet) {
                route.removeTopic(groupName, topicName);
            }
        } catch (Exception e) {
            log.error("delete topic error", e);
        } finally {
            route.unlockWrite();
        }
    }

    private void getUnitTopic(TopicList topicList, Map.Entry<String, Map<String, Topic>> topicEntry) {
        String topicName = topicEntry.getKey();
        Map<String, Topic> topicMap = topicEntry.getValue();

        if (MapUtil.isEmpty(topicMap)) {
            return;
        }

        int sysFlag = topicMap.values()
            .iterator()
            .next()
            .getTopicSysFlag();

        if (TopicSysFlag.hasUnitFlag(sysFlag)) {
            topicList.getTopicList().add(topicName);
        }
    }

    private void getTopicByGroup(TopicList topicList, String groupName) {
        for (Map.Entry<String, Map<String, Topic>> entry: route.getTopicMap().entrySet()) {
            // find topic by groupName
            Topic topic = entry.getValue().get(groupName);
            if (topic == null) {
                continue;
            }

            // add topic to topicList
            topicList.getTopicList().add(entry.getKey());
        }
    }

    private void getFirstAddressFromGroupMap(TopicList topicList) {
        if (route.isGroupEmpty()) {
            return;
        }

        for (Map.Entry<String, GroupInfo> entry: route.getGroupMap().entrySet()) {
            Map<Long, String> addrMap = entry.getValue().getBrokerAddrs();
            if (MapUtil.isEmpty(addrMap)) {
                continue;
            }

            Iterator<Long> iterator = addrMap.keySet().iterator();
            topicList.setBrokerAddr(addrMap.get(iterator.next()));

            // return the first address found
            break;
        }
    }

    private boolean getQueueList(RouteInfo routeInfo, String topicName) {
        routeInfo.setTopicName(topicName);

        Map<String, Topic> topicMap = route.getTopicMap().get(topicName);
        if (MapUtil.isEmpty(topicMap)) {
            return false;
        }

        MessageType messageType = null;
        List<QueueInfo> queueInfoList = new ArrayList<>();
        for (Map.Entry<String, Topic> entry: topicMap.entrySet()) {
            QueueInfo queueInfo = QueueInfo.from(entry.getKey(), entry.getValue());
            queueInfoList.add(queueInfo);

            if (null == messageType) {
                messageType = entry.getValue().getTopicType();
            }
        }

        routeInfo.setMessageType(messageType);
        routeInfo.setQueueDatas(queueInfoList);
        return true;
    }

    private void getGroupInfo(RouteInfo routeInfo, String topicName) {
        Set<String> groupSet = route.getTopicByGroup(topicName);
        for (String groupName : groupSet) {
            GroupInfo groupInfo = route.getGroup(groupName);
            if (groupInfo == null) {
                continue;
            }
            GroupInfo groupClone = new GroupInfo(groupInfo);
            routeInfo.getBrokerDatas().add(groupClone);

            getFilterInfo(routeInfo, groupClone);
        }
    }

    private void getFilterInfo(RouteInfo routeInfo, GroupInfo groupClone) {
        if (route.isFilterEmpty()) {
            return;
        }

        for (String address: groupClone.getBrokerAddrs().values()) {
            StoreInfo storeInfo = new StoreInfo(groupClone.getCluster(), address);
            List<String> filterList = route.getFilter(storeInfo);
            if (CollectionUtil.isEmpty(filterList)) {
                continue;
            }
            routeInfo.getFilterServerTable().put(address, filterList);
        }

    }

    private void getQueueMap(RouteInfo routeInfo, String topicName) {
        Map<String, TopicQueueMappingInfo> map = route.getQueueMap(topicName);
        if (MapUtil.isEmpty(map)) {
            return;
        }

        routeInfo.setTopicQueueMappingByBroker(map);
    }

    private void handleActingMaster(RouteInfo routeInfo, String topicName) {

    }

}
