package com.wolfmq.broker.domain.consumer.renew;

import com.wolfmq.rpc.common.core.channel.ChannelHelper;
import com.wolfmq.common.core.event.ConsumerEvent;
import com.wolfmq.common.server.ClientChannelInfo;
import com.wolfmq.common.domain.consumer.receipt.ReceiptHandleGroupKey;
import com.wolfmq.common.domain.consumer.hook.ConsumerListener;
import com.wolfmq.common.domain.consumer.receipt.ReceiptHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiptListener implements ConsumerListener {
    private final ReceiptHandler receiptHandler;

    public ReceiptListener(ReceiptHandler receiptHandler) {
        this.receiptHandler = receiptHandler;
    }

    @Override
    public void handle(ConsumerEvent event, String group, Object... args) {
        if (!ConsumerEvent.CLIENT_UNREGISTER.equals(event)) {
            return;
        }

        if (null == args ||  args.length < 1) {
            return;
        }

        if (!(args[0] instanceof ClientChannelInfo channelInfo)) {
            return;
        }

        if (ChannelHelper.isRemote(channelInfo.getChannel())) {
            return;
        }

        ReceiptHandleGroupKey key = new ReceiptHandleGroupKey(
            channelInfo.getChannel(),
            group
        );
        receiptHandler.removeGroup(key);

        log.info("clear receipt handle when client unregister, group:{}, ClientChannelInfo:{}",
            group, channelInfo);
    }
}
