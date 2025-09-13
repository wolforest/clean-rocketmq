package cn.coderule.minimq.domain.domain.consumer.ack.store;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.ack.AckInfo;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.domain.domain.meta.topic.KeyBuilder;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AckMessage implements Serializable {
    private RequestContext requestContext;

    private String storeGroup;

    private String receiptStr;
    private ReceiptHandle receiptHandle;
    private AckInfo ackInfo;
    private int reviveQueueId;
    private long invisibleTime;

    public boolean isConsumeOrderly() {
        return KeyBuilder.POP_ORDER_REVIVE_QUEUE == reviveQueueId;
    }
}
