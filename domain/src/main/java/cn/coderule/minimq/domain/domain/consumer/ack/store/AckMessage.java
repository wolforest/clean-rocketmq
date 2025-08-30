package cn.coderule.minimq.domain.domain.consumer.ack.store;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import java.io.Serializable;
import lombok.Data;

@Data
public class AckMessage implements Serializable {
    private RequestContext requestContext;

    private String storeGroup;

    private AckInfo ackInfo;
    private int reviveQueueId;
    private long invisibleTime;
}
