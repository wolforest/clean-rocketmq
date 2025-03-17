package cn.coderule.minimq.registry.domain.store;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.RegistryConfig;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.registry.domain.store.model.Route;
import cn.coderule.minimq.rpc.registry.protocol.body.TopicList;
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

    public TopicList getTopicList() {
        TopicList topicList = new TopicList();

        try {
            route.lockRead();
            topicList.setTopicList(route.getTopicMap().keySet());
        } catch (Exception e) {
            log.error("register topic error", e);
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
            log.error("register topic error", e);
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
            log.error("register topic error", e);
        } finally {
            route.unlockWrite();
        }
    }

}
