package cn.coderule.minimq.broker.domain.consumer.renew;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumeHookManager;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumerRegister;
import cn.coderule.minimq.broker.infra.store.SubscriptionStore;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;
import cn.coderule.minimq.domain.service.broker.consume.ReceiptHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultReceiptHandler implements ReceiptHandler, Lifecycle {
    private ConsumeHookManager hookManager;
    private SubscriptionStore subscriptionStore;
    private ConsumerRegister consumerRegister;

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void addReceipt(RequestContext context, MessageReceipt messageReceipt) {

    }

    @Override
    public MessageReceipt removeReceipt(RequestContext context, MessageReceipt messageReceipt) {
        return null;
    }

    @Override
    public void clearGroup(ReceiptHandleGroupKey key) {

    }
}
