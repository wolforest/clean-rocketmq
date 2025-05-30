
package cn.coderule.minimq.domain.domain.model.consumer.receipt;

import java.io.Serializable;
import lombok.Getter;

@Getter
public class MessageIdReceipt implements Serializable {

    private final ReceiptHandle receiptHandle;
    private final String messageId;

    public MessageIdReceipt(ReceiptHandle receiptHandle, String messageId) {
        this.receiptHandle = receiptHandle;
        this.messageId = messageId;
    }
}
