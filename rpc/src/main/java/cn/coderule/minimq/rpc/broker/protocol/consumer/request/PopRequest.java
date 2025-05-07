package cn.coderule.minimq.rpc.broker.protocol.consumer.request;

import cn.coderule.minimq.rpc.broker.protocol.consumer.PopFilter;
import cn.coderule.minimq.rpc.broker.protocol.heartbeat.SubscriptionData;
import cn.coderule.minimq.rpc.common.core.RequestContext;
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
