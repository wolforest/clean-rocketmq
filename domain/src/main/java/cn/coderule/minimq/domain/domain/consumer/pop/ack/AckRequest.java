package cn.coderule.minimq.domain.domain.model.consumer.pop.ack;

import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.consumer.receipt.MessageIdReceipt;
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
