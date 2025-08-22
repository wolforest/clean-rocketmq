
package cn.coderule.minimq.domain.domain.consumer.revive;

import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewEvent implements Serializable {
    protected ReceiptHandleGroupKey key;
    protected MessageReceipt messageReceipt;
    protected long renewTime;
    protected EventType eventType;
    protected CompletableFuture<AckResult> future;

    public enum EventType {
        RENEW,
        STOP_RENEW,
        CLEAR_GROUP
    }

    public MessageReceipt getMessageReceiptHandle() {
        return messageReceipt;
    }

}
