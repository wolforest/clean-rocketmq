
package cn.coderule.minimq.domain.domain.consumer.receipt;

import java.util.Map;
import java.util.Set;

public interface ReceiptHandler {
    void addReceipt(MessageReceipt messageReceipt);
    MessageReceipt removeReceipt(MessageReceipt messageReceipt);

    void removeGroup(ReceiptHandleGroupKey key);
    Set<Map.Entry<ReceiptHandleGroupKey, ReceiptHandleGroup>> getEntrySet();
}
