package cn.coderule.wolfmq.domain.domain.consumer.ack.broker;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandle;
import java.io.Serializable;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvisibleRequest implements Serializable {
    private RequestContext requestContext;

    private String messageId;
    private String receiptStr;
    private String topicName;
    private String groupName;

    private long invisibleTime;

    @Builder.Default
    private long timeout = Duration.ofSeconds(2).toMillis();
}
