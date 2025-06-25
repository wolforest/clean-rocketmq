package cn.coderule.minimq.domain.domain.consumer.ack.store;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.ack.AckMsg;
import java.io.Serializable;
import lombok.Data;

@Data
public class AckRequest implements Serializable {
    private RequestContext requestContext;

    private String storeGroup;

    private AckMsg ackMsg;
    private int reviveQueueId;
    private long invisibleTime;
}
