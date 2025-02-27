package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.domain.config.BrokerConfig;
import cn.coderule.minimq.domain.model.MessageQueue;
import java.util.Set;

/**
 * load route info from name server
 *
 */
public class RouteService {
    private BrokerConfig brokerConfig;
    private TopicService topicService;

    // private RegistryClient registryClient;

    public Set<MessageQueue> getOrCreateRoute(String topic) {
        return null;
    }

    public Set<MessageQueue> getRoute(String topic) {
        return null;
    }
}
