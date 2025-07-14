package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.service.broker.consume.ReceiptHandler;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultReceiptHandler implements ReceiptHandler, Lifecycle {
    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void addReceiptHandle(RequestContext context, Channel channel, MessageReceipt messageReceipt) {

    }

    @Override
    public MessageReceipt removeReceiptHandle(RequestContext context, Channel channel,MessageReceipt messageReceipt) {
        return null;
    }
}
