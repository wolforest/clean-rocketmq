package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.model.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultReceiptHandler implements ReceiptHandler, Lifecycle {
    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void addReceiptHandle(RequestContext context, Channel channel, String group, String msgID,
        MessageReceipt messageReceipt) {

    }

    @Override
    public MessageReceipt removeReceiptHandle(RequestContext context, Channel channel, String group, String msgID,
        String receiptHandle) {
        return null;
    }
}
