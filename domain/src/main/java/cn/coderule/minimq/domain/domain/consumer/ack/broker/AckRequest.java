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

    @Builder.Default
    private List<MessageIdReceipt> receiptList = new ArrayList<>();
    private String topicName;
    private String groupName;
    private int queueId;

    private long offset;
    private long invisibleTime;
    private String extraInfo;

    @Builder.Default
    private long timeout = Duration.ofSeconds(2).toMillis();

    public void addReceipt(String messageId, ReceiptHandle receiptHandle) {
        receiptList.add(
            new MessageIdReceipt(receiptHandle, messageId)
        );
    }

    public String getFirstMessageId() {
        if (receiptList.isEmpty()) {
            return null;
        }
        return receiptList.get(0).getMessageId();
    }

    public ReceiptHandle getFirstReceiptHandle() {
        if (receiptList.isEmpty()) {
            return null;
        }
        return receiptList.get(0).getReceiptHandle();
    }
}
