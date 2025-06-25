package cn.coderule.minimq.domain.domain.model.meta;

import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class OffsetRequest implements Serializable {
    private RequestContext requestContext;
    private long timeout;

    private MessageQueue messageQueue;
    private String consumerGroup;

}
