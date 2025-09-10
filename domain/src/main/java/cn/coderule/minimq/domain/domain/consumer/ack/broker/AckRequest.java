package cn.coderule.minimq.domain.domain.consumer.ack.broker;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageIdReceipt;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AckRequest implements Serializable {
    private RequestContext requestContext;

    private ReceiptHandle receiptHandle;
    private String messageId;
    private String topicName;
    private String groupName;
    private int queueId;

    private long offset;
    private long invisibleTime;
    private String extraInfo;

    @Builder.Default
    private long timeout = Duration.ofSeconds(2).toMillis();


}
