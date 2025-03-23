package cn.coderule.minimq.rpc.registry.protocol.route;

import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.rpc.common.protocol.codec.RpcSerializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class TopicInfo extends RpcSerializable {
    private String groupName;
    private Topic topic;

    public RouteInfo toRouteInfo() {
        RouteInfo routeInfo = new RouteInfo();

        QueueInfo queueInfo = QueueInfo.from(groupName, topic);
        routeInfo.getQueueDatas().add(queueInfo);

        return routeInfo;
    }
}
