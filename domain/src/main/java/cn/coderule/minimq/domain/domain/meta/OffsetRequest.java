package cn.coderule.minimq.domain.domain.meta;

import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class OffsetRequest implements Serializable {
    private RequestContext requestContext;
    private long timeout;

    private MessageQueue messageQueue;
    private String consumerGroup;

}
