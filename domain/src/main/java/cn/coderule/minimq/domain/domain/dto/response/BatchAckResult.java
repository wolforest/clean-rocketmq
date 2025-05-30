
package cn.coderule.minimq.domain.domain.dto.response;

import cn.coderule.minimq.domain.domain.exception.BrokerException;
import cn.coderule.minimq.domain.domain.model.consumer.receipt.MessageIdReceipt;

public class BatchAckResult {

    private final MessageIdReceipt messageIdReceipt;
    private AckResult ackResult;
    private BrokerException brokerException;

    public BatchAckResult(MessageIdReceipt messageIdReceipt,
        AckResult ackResult) {
        this.messageIdReceipt = messageIdReceipt;
        this.ackResult = ackResult;
    }

    public BatchAckResult(MessageIdReceipt messageIdReceipt,
        BrokerException brokerException) {
        this.messageIdReceipt = messageIdReceipt;
        this.brokerException = brokerException;
    }

    public MessageIdReceipt getReceiptHandleMessage() {
        return messageIdReceipt;
    }

    public AckResult getAckResult() {
        return ackResult;
    }

    public BrokerException getBrokerException() {
        return brokerException;
    }
}
