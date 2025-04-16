package cn.coderule.minimq.broker.domain.route;

import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.domain.service.store.api.TopicStore;
import cn.coderule.minimq.rpc.registry.protocol.route.QueueInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;

public class RouteMocker {
    private final BrokerConfig brokerConfig;
    private final TopicStore topicStore;

    public RouteMocker(BrokerConfig brokerConfig, TopicStore topicStore) {
        this.brokerConfig = brokerConfig;
        this.topicStore = topicStore;
    }

    public RouteInfo getRoute(String topicName) {
        Topic topic = topicStore.getTopic(topicName);
         return toRoute(topic);
    }

    public RouteInfo toRoute(Topic topic) {
        RouteInfo result = new RouteInfo();
        if (topic == null) {
            return result;
        }

        QueueInfo queueInfo = QueueInfo.from(brokerConfig.getGroup(), topic);
        result.getQueueDatas().add(queueInfo);

        return result;
    }
}
