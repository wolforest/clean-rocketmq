package cn.coderule.minimq.rpc.broker.protocol.consumer.request;

import cn.coderule.minimq.domain.domain.model.consumer.receipt.MessageIdReceipt;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class AckRequest implements Serializable {
    private RequestContext requestContext;
    private List<MessageIdReceipt> receiptList;
    private String topicName;
    private String groupName;
    private long invisibleTime;
    private long timeout;
}
