package com.wolfmq.common.domain.consumer.ack.store;

import com.wolfmq.common.server.RequestContext;
import com.wolfmq.common.domain.consumer.ack.AckInfo;
import com.wolfmq.common.domain.consumer.receipt.ReceiptHandle;
import com.wolfmq.common.domain.meta.topic.KeyBuilder;
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
    private long commitOffset;

    @Builder.Default
    private boolean isOrderly = false;

    public boolean isConsumeOrderly() {
        return KeyBuilder.POP_ORDER_REVIVE_QUEUE == reviveQueueId;
    }
}
