package cn.coderule.minimq.domain.domain.model.consumer.request;

import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class OffsetRequest implements Serializable {
    private RequestContext requestContext;
    private long timeout;

    private MessageQueue messageQueue;
    private String consumerGroup;

}
