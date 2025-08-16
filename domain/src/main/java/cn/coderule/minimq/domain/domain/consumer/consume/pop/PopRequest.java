package cn.coderule.minimq.domain.domain.consumer.consume.pop;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.service.broker.consume.PopFilter;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopRequest implements Serializable {
    private RequestContext requestContext;
    private long timeout;
    private String attemptId;

    private String topicName;
    private String consumerGroup;
    private String storeGroup;

    private int maxNum;
    private long invisibleTime;
    private long pollTime;
    private long remainTime;
    private int initMode;

    private boolean autoRenew;
    private boolean fifo;
    private SubscriptionData subscriptionData;
    private PopFilter filter;
}
