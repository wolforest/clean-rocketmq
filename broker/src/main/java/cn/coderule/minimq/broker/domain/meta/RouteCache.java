package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;

@Data
public class RouteCache implements Serializable {
    // topicName -> routeInfo
    private final ConcurrentMap<String, RouteInfo> routeMap;
    // groupName -> groupNo -> address
    private final ConcurrentMap<String, Map<Long, String>> addressMap;
    // topicName -> Set<messageQueue>
    private final ConcurrentMap<String, Set<MessageQueue>> queueMap;


}
