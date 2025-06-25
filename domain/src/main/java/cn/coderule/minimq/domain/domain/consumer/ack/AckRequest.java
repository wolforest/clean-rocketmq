package cn.coderule.minimq.domain.domain.consumer.ack;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageIdReceipt;
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
