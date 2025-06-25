
package cn.coderule.minimq.domain.service.broker.consume;

import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import io.netty.channel.Channel;


public interface ReceiptHandler {
    void addReceiptHandle(RequestContext context, Channel channel, MessageReceipt messageReceipt);

    MessageReceipt removeReceiptHandle(RequestContext context, Channel channel, MessageReceipt messageReceipt);
}
