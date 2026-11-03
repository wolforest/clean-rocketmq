package com.wolfmq.common.server.registry.route;

import com.wolfmq.common.server.broker.RequestType;
import com.wolfmq.common.domain.meta.topic.Topic;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TopicInfo implements Serializable {
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
