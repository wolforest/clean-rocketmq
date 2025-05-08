package cn.coderule.minimq.domain.domain.model.consumer.request;

import cn.coderule.minimq.domain.domain.model.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import java.io.Serializable;
import lombok.Data;

@Data
public class InvisibleRequest implements Serializable {
    private RequestContext requestContext;
    private ReceiptHandle receiptHandle;
    private String messageId;
    private String topicName;
    private String groupName;
    private long invisibleTime;
    private long timeout;
}
