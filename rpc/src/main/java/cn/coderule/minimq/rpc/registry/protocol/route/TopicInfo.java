package cn.coderule.minimq.rpc.registry.protocol.route;

import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.rpc.rpc.core.enums.RequestType;
import cn.coderule.minimq.rpc.rpc.protocol.codec.RpcSerializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class TopicInfo extends RpcSerializable {
    private String groupName;
    private Topic topic;

    @Builder.Default
    private int registerTimeout = 3_000;
    private RequestType registerType;

    public RouteInfo toRouteInfo() {
        RouteInfo routeInfo = new RouteInfo();

        QueueInfo queueInfo = QueueInfo.from(groupName, topic);
        routeInfo.getQueueDatas().add(queueInfo);

        return routeInfo;
    }
}
