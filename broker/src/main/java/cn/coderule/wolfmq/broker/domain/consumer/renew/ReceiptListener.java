package cn.coderule.wolfmq.broker.domain.consumer.renew;

import cn.coderule.wolfmq.rpc.common.core.channel.ChannelHelper;
import cn.coderule.wolfmq.domain.core.enums.consume.ConsumerEvent;
import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;
import cn.coderule.wolfmq.domain.domain.consumer.hook.ConsumerListener;
import cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;
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
