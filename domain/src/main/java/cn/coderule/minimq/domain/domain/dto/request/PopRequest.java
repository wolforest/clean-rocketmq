package cn.coderule.minimq.domain.domain.dto.request;

import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.cluster.heartbeat.SubscriptionData;
import cn.coderule.minimq.domain.domain.model.consumer.PopFilter;
import java.io.Serializable;
import lombok.Data;

@Data
public class PopRequest implements Serializable {
    private RequestContext requestContext;
    private long timeout;
    private String attemptId;

    private String topicName;
    private String consumerGroup;
    private int maxNum;
    private long invisibleTime;
    private long pollTime;
    private int initMode;

    private boolean fifo;
    private SubscriptionData subscriptionData;
    private PopFilter filter;
}
