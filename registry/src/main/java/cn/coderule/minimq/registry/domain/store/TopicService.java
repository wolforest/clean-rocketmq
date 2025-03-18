package cn.coderule.minimq.registry.domain.store;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicList;
import cn.coderule.minimq.rpc.registry.protocol.route.QueueInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TopicService {
    private final RegistryConfig config;
    private final Route route;

    public TopicService(RegistryConfig config, Route route) {
        this.route = route;
        this.config = config;
    }

    private boolean setQueueList(RouteInfo routeInfo, String topicName) {
        Map<String, Topic> topicMap = route.getTopicMap().get(topicName);
        if (MapUtil.isEmpty(topicMap)) {
            return false;
        }

        List<QueueInfo> queueInfoList = new ArrayList<>();
        for (Map.Entry<String, Topic> entry: topicMap.entrySet()) {
            QueueInfo queueInfo = QueueInfo.from(entry.getKey(), entry.getValue());
            queueInfoList.add(queueInfo);
        }

        routeInfo.setQueueList(queueInfoList);
        return true;
    }

    private void setGroupInfo(RouteInfo routeInfo, String topicName) {

    }

    public RouteInfo getRoute(String topicName) {
        RouteInfo routeInfo = new RouteInfo();

        try {
            route.lockRead();

            if (!setQueueList(routeInfo, topicName)) {
                return null;
            }

            setGroupInfo(routeInfo, topicName);
        } catch (Exception e) {
            log.error("getRoute error", e);
        } finally {
            route.unlockRead();
        }

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

}
