package cn.coderule.wolfmq.domain.domain.consumer.consume.pop;

import cn.coderule.wolfmq.domain.core.enums.consume.ConsumeStrategy;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.server.heartbeat.SubscriptionData;
import cn.coderule.wolfmq.domain.domain.consumer.hook.PopFilter;
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
    private int queueId;

    private int maxNum;
    private long invisibleTime;
    private long pollTime;
    private long remainTime;

    private ConsumeStrategy consumeStrategy;

    private boolean autoRenew;
    private boolean fifo;
    private SubscriptionData subscriptionData;
    private PopFilter filter;
}
