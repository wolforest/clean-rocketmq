
package cn.coderule.minimq.domain.service.broker.consume;

import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;

public interface ReceiptHandler {
    void addReceipt(RequestContext context, MessageReceipt messageReceipt);
    MessageReceipt removeReceipt(RequestContext context, MessageReceipt messageReceipt);

    void clearGroup(ReceiptHandleGroupKey key);
}
