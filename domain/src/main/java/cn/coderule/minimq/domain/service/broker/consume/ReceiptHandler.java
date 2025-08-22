
package cn.coderule.minimq.domain.service.broker.consume;

import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;

public interface ReceiptHandler {
    void addReceipt(MessageReceipt messageReceipt);
    MessageReceipt removeReceipt(MessageReceipt messageReceipt);

    void removeGroup(ReceiptHandleGroupKey key);
}
