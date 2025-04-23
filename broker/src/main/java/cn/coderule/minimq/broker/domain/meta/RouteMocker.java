package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.config.TopicConfig;
import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.QueueInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RouteMocker {
    private final BrokerConfig brokerConfig;
    private final TopicConfig topicConfig;
    private final TopicStore topicStore;

    public RouteMocker(BrokerConfig brokerConfig, TopicConfig topicConfig, TopicStore topicStore) {
        this.topicConfig = topicConfig;
        this.brokerConfig = brokerConfig;
        this.topicStore = topicStore;
    }

    public RouteInfo getRoute(String topicName) {
        Topic topic = topicStore.getTopic(topicName);
        if (topic == null) {
            topic = createTopic(topicName);
        }
        return toRoute(topic);
    }

    private Topic createTopic(String topicName) {
        if (!topicConfig.isEnableAutoCreation()) {
            return null;
        }

        Topic topic = Topic.builder()
            .topicName(topicName)
            .readQueueNums(topicConfig.getDefaultQueueNum())
            .writeQueueNums(topicConfig.getDefaultQueueNum())
            .build();

        try {
            topicStore.saveTopic(topic);
            return topic;
        } catch (Exception e) {
            log.error("create topic={} error", topicName, e);
            return null;
        }
    }

    private RouteInfo toRoute(Topic topic) {
        RouteInfo result = new RouteInfo();
        if (topic == null) {
            return result;
        }

        result.setTopicName(topic.getTopicName());
        result.setMessageType(topic.getTopicType());

        QueueInfo queueInfo = QueueInfo.from(brokerConfig.getGroup(), topic);
        result.getQueueDatas().add(queueInfo);

        GroupInfo groupInfo = createGroupInfo();
        result.getBrokerDatas().add(groupInfo);

        return result;
    }

    private GroupInfo createGroupInfo() {
        GroupInfo groupInfo = new GroupInfo(brokerConfig.getCluster(), brokerConfig.getName());

        Map<Long, String> addressMap = new HashMap<>();
        String address = brokerConfig.getHost() + ":" + brokerConfig.getPort();
        addressMap.put(brokerConfig.getGroupNo(), address);

        groupInfo.setBrokerAddrs(addressMap);
        return groupInfo;
    }

}
